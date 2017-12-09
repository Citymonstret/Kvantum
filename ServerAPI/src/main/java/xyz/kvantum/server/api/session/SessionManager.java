/*
 *
 *    Copyright (C) 2017 IntellectualSites
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Synchronized;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.Cookie;
import xyz.kvantum.server.api.response.HeaderProvider;
import xyz.kvantum.server.api.response.ResponseCookie;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.ProviderFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({ "WeakerAccess", "unused" })
@AllArgsConstructor
public final class SessionManager implements ProviderFactory<ISession>
{

    private final Cache<String, ISession> sessions = CacheBuilder.newBuilder()
            .maximumSize( CoreConfig.Cache.cachedSessionsMaxItems )
            .expireAfterAccess( CoreConfig.Sessions.sessionTimeout, TimeUnit.SECONDS ).build();

    private final ISessionCreator sessionCreator;
    private final ISessionDatabase sessionDatabase;

    private static final String SESSION_KEY = "intellectual_session";
    private static final String SESSION_PASS = "intellectual_key";

    private ISession createSession(@NonNull final AbstractRequest r)
    {
        Assert.isValid( r );

        final String sessionID = UUID.randomUUID().toString();
        if ( CoreConfig.debug )
        {
            Message.SESSION_SET.log( SESSION_KEY, sessionID );
        }
        final ISession session = createSession( sessionID );
        saveCookies( r, session, sessionID );
        return session;
    }

    private void saveCookies(@NonNull final AbstractRequest r,
                             @NonNull final ISession session,
                             @NonNull final String sessionID)
    {
        r.postponedCookies.add( ResponseCookie.builder()
                .cookie( SESSION_KEY ).value( sessionID ).httpOnly( true ).build() );
        r.postponedCookies.add( ResponseCookie.builder().cookie( SESSION_PASS )
                .value( session.getSessionKey() ).httpOnly( true ).build() );
        // Make sure that the cookies aren't duplicated
        r.getCookies().removeAll( SESSION_KEY );
        r.getCookies().removeAll( SESSION_PASS );
        r.getCookies().put( SESSION_KEY, new Cookie( SESSION_KEY, sessionID ) );
        r.getCookies().put( SESSION_PASS, new Cookie( SESSION_PASS, session.getSessionKey() ) );
    }

    @Synchronized
    private ISession createSession(@NonNull final String sessionID)
    {
        Assert.notEmpty( sessionID );

        final ISession session = sessionCreator.createSession().set( "id", sessionID );
        this.sessions.put( sessionID, session );
        this.sessionDatabase.storeSession( session );
        return session;
    }

    public void deleteSession(@NonNull final AbstractRequest r, @NonNull final HeaderProvider re)
    {
        Assert.notNull( r, re );

        re.getHeader().removeCookie( SESSION_KEY );
    }

    @Synchronized
    public Optional<ISession> getSession(@NonNull final AbstractRequest r)
    {
        Assert.isValid( r );

        ISession session = null;

        String sessionCookie = null;
        String sessionPassCookie = null;

        //
        // STEP 1: Check if the client provides the required headers
        //
        for ( final Cookie cookie : r.getCookies().values() )
        {
            if ( sessionCookie == null && cookie.getName().equalsIgnoreCase( SESSION_KEY ) )
            {
                sessionCookie = cookie.getValue();
            } else if ( sessionPassCookie == null && cookie.getName().equalsIgnoreCase( SESSION_PASS ) )
            {
                sessionPassCookie = cookie.getValue();
            }
            if ( sessionCookie != null && sessionPassCookie != null )
            {
                break;
            }
        }

        //
        // STEP 2 (1): Validate the provided headers
        //
        if ( sessionCookie != null && sessionPassCookie != null )
        {
            if ( ( session = this.sessions.getIfPresent( sessionCookie ) ) != null )
            {
                if ( CoreConfig.debug )
                {
                    Message.SESSION_FOUND.log( session, sessionCookie, r );
                }

                long difference = ( System.currentTimeMillis() - (long) session.get( "last_active" ) ) / 1000;
                if ( difference >= CoreConfig.Sessions.sessionTimeout )
                {
                    if ( CoreConfig.debug )
                    {
                        Logger.debug( "Deleted outdated session: %s", session );
                    }
                    this.sessions.invalidate( sessionCookie );
                    this.sessionDatabase.deleteSession( sessionCookie );
                    session = null;
                }
            } else
            {
                final SessionLoad load = sessionDatabase.isValid( sessionCookie );
                if ( load != null )
                {
                    session = createSession( sessionCookie );
                    session.setSessionKey( load.getSessionKey() );
                    return Optional.of( session );
                } else
                {
                    // Session isn't valid, remove old cookie
                    ServerImplementation.getImplementation()
                            .log( "Deleting invalid session cookie for request %s", r );
                    session = null;
                }
            }

            if ( session != null && !session.getSessionKey().equalsIgnoreCase( sessionPassCookie ) )
            {
                if ( CoreConfig.debug )
                {
                    Logger.debug( "Deleted session: %s (Cause: %s)", session, "Wrong session key" );
                }
                this.sessions.invalidate( sessionCookie );
                this.sessionDatabase.deleteSession( sessionCookie );
                session = null;
            }
        }

        //
        // STEP 2 (2): Create a new session
        //
        if ( session == null )
        {
            session = createSession( r );
        }

        return Optional.ofNullable( session );
    }

    public Optional<ISession> getSession(final String sessionID)
    {
        return Optional.ofNullable( sessions.getIfPresent( sessionID ) );
    }

    public void setSessionLastActive(final String sessionID)
    {
        final Optional<ISession> session = getSession( sessionID );
        if ( session.isPresent() )
        {
            session.get().set( "last_active", System.currentTimeMillis() );
            this.sessionDatabase.updateSession( sessionID );
        }
    }

    @Override
    public Optional<ISession> get(final AbstractRequest r)
    {
        return getSession( r );
    }

    public String providerName()
    {
        return "session";
    }

}
