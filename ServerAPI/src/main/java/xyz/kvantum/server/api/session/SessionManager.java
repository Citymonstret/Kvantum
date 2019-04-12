/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import lombok.val;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.Cookie;
import xyz.kvantum.server.api.response.HeaderProvider;
import xyz.kvantum.server.api.response.ResponseCookie;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.ProviderFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Manager for {@link ISession sessions}
 * {@inheritDoc}
 */
@SuppressWarnings({"WeakerAccess", "unused"}) @AllArgsConstructor public final class SessionManager
    implements ProviderFactory<ISession> {

    private static final AsciiString SESSION_KEY = AsciiString.of("intellectual_session");
    private static final AsciiString SESSION_PASS = AsciiString.of("intellectual_key");

    private final ISessionCreator sessionCreator;
    private final ISessionDatabase sessionDatabase;
    private final Cache<AsciiString, ISession> sessions =
        Caffeine.newBuilder().maximumSize(CoreConfig.Cache.cachedSessionsMaxItems)
            .removalListener(this::saveSession)
            .expireAfterAccess(CoreConfig.Sessions.sessionTimeout, TimeUnit.SECONDS).build();

    private ISession createSession(final AbstractRequest r) {
        Assert.isValid(r);

        final AsciiString sessionID = AsciiString.randomUUIDAsciiString();
        if (CoreConfig.debug) {
            Message.SESSION_SET.log(SESSION_KEY, sessionID);
        }
        final ISession session = createSession(sessionID);
        saveCookies(r, session, sessionID);
        return session;
    }

    private void saveSession(final AsciiString key, final ISession value,
        final RemovalCause cause) {
        if (cause != RemovalCause.EXPLICIT) {
            this.sessionDatabase.updateSession(key);
        }
    }

    private void saveCookies(final AbstractRequest r, final ISession session,
        final AsciiString sessionID) {
        r.postponedCookies.add(
            ResponseCookie.builder().cookie(SESSION_KEY).value(sessionID).httpOnly(true).build());
        r.postponedCookies.add(
            ResponseCookie.builder().cookie(SESSION_PASS).value(session.getSessionKey())
                .httpOnly(true).build());
        // Make sure that the cookies aren't duplicated
        r.getCookies().remove(SESSION_KEY);
        r.getCookies().remove(SESSION_PASS);
        r.getCookies().put(SESSION_KEY, new Cookie(SESSION_KEY, sessionID));
        r.getCookies().put(SESSION_PASS, new Cookie(SESSION_PASS, session.getSessionKey()));
    }

    @Synchronized private ISession createSession(final AsciiString sessionID) {
        final ISession session = sessionCreator.createSession().set("id", sessionID);
        this.sessions.put(sessionID, session);
        this.sessionDatabase.storeSession(session);
        return session;
    }

    public void deleteSession(final AbstractRequest r, final HeaderProvider re) {
        re.getHeader().removeCookie(SESSION_KEY);
    }

    /**
     * Get a session from a given {@link AbstractRequest request}
     *
     * @param r Request to query from
     * @return (Optional) session
     */
    @Synchronized public Optional<ISession> getSession(final AbstractRequest r) {
        Assert.isValid(r);

        ISession session = null;

        AsciiString sessionCookie = null;
        AsciiString sessionPassCookie = null;

        //
        // STEP 1: Check if the client provides the required headers
        //
        findCookie:
        {
            final val keyCookieList = r.getCookies().get(SESSION_KEY);
            final val passCookieList = r.getCookies().get(SESSION_PASS);
            if (keyCookieList.isEmpty() || passCookieList.isEmpty()) {
                break findCookie;
            }
            sessionCookie = keyCookieList.get(0).getValue();
            sessionPassCookie = passCookieList.get(0).getValue();
        }

        //
        // STEP 2 (1): Validate the provided headers
        //
        if (sessionCookie != null && sessionPassCookie != null) {
            //
            // Check the session cache
            //
            if ((session = this.sessions.getIfPresent(sessionCookie)) != null) {
                if (CoreConfig.debug) {
                    Message.SESSION_FOUND.log(session, sessionCookie, r);
                }
                //
                // Make sure it isn't expired
                //
                long difference =
                    (System.currentTimeMillis() - (long) session.get("last_active")) / 1000;
                if (difference >= CoreConfig.Sessions.sessionTimeout) {
                    if (CoreConfig.debug) {
                        Message.SESSION_DELETED_OUTDATED.log(session);
                    }
                    session.setDeleted();
                    this.sessions.invalidate(sessionCookie);
                    this.sessionDatabase.deleteSession(sessionCookie);
                    session = null;
                }
            } else {
                //
                // If it cannot be found, try to load it from the database
                //
                final SessionLoad load = sessionDatabase.isValid(sessionCookie);
                if (load != null) {
                    session = createSession(sessionCookie);
                    session.setSessionKey(AsciiString.of(load.getSessionKey(), false));
                    return Optional.of(session);
                } else {
                    // Session isn't valid, remove old cookie
                    ServerImplementation.getImplementation()
                        .log("Deleting invalid session cookie for request {}", r);
                    session = null;
                }
            }

            //
            // Make sure that the session has the correct passcode
            //
            if (session != null && !session.getSessionKey().equalsIgnoreCase(sessionPassCookie)) {
                if (CoreConfig.debug) {
                    Message.SESSION_DELETED_OTHER
                        .log(session, Message.SESSION_KEY_INVALID.toString());
                }
                session.setDeleted();
                this.sessions.invalidate(sessionCookie);
                this.sessionDatabase.deleteSession(sessionCookie);
                session = null;
            }
        }

        //
        // STEP 2 (2): Create a new session
        //
        if (session == null) {
            session = createSession(r);
        }

        return Optional.ofNullable(session);
    }

    /**
     * Get a session from a given session ID
     *
     * @param sessionID Session ID
     * @return (Optional) session
     */
    public Optional<ISession> getSession(final AsciiString sessionID) {
        return Optional.ofNullable(sessions.getIfPresent(sessionID));
    }

    /**
     * Update the last active field for a given session
     *
     * @param sessionID Session ID to update information for
     */
    public void setSessionLastActive(final AsciiString sessionID) {
        final Optional<ISession> session = getSession(sessionID);
        session.ifPresent(iSession -> iSession.set("last_active", System.currentTimeMillis()));
    }

    @Override public Optional<ISession> get(final AbstractRequest r) {
        return getSession(r);
    }

    @Override public String providerName() {
        return "session";
    }

}
