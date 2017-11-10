/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.session;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.Cookie;
import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.response.HeaderProvider;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AllArgsConstructor;

import java.io.BufferedOutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({ "unused", "WeakerAccess" })
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
    
    private ISession createSession(final Request r, final BufferedOutputStream out)
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

    private void saveCookies(final Request r, final ISession session, final String sessionID)
    {
        r.postponedCookies.put( SESSION_KEY, sessionID );
        r.postponedCookies.put( SESSION_PASS, session.getSessionKey() );
        // Make sure that the cookies aren't duplicated
        r.getCookies().removeAll( SESSION_KEY );
        r.getCookies().removeAll( SESSION_PASS );
        r.getCookies().put( SESSION_KEY, new Cookie( SESSION_KEY, sessionID ) );
        r.getCookies().put( SESSION_PASS, new Cookie( SESSION_PASS, session.getSessionKey() ) );
    }

    private ISession createSession(final String sessionID)
    {
        Assert.notEmpty( sessionID );

        final ISession session = sessionCreator.createSession().set( "id", sessionID );
        this.sessions.put( sessionID, session );
        this.sessionDatabase.storeSession( session );
        return session;
    }

    public void deleteSession(final Request r, final HeaderProvider re)
    {
        Assert.notNull( r, re );

        re.getHeader().setCookie( SESSION_KEY, "deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT" );
    }

    public Optional<ISession> getSession(final Request r, final BufferedOutputStream out)
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
            if ( this.sessions.getIfPresent( sessionCookie ) != null )
            {
                session = sessions.getIfPresent( sessionCookie );

                assert session != null;

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
                if ( sessionDatabase.isValid( sessionCookie ) )
                {
                    final Map<String, String> sessionLoad = sessionDatabase.getSessionLoad( sessionCookie );
                    session = createSession( sessionCookie );
                    session.setSessionKey( sessionLoad.get( "sessionKey" ) );
                    return Optional.of( session );
                } else
                {
                    // Session isn't valid, remove old cookie
                    if ( out != null )
                    {
                        ServerImplementation.getImplementation()
                                .log( "Deleting invalid session cookie for request %s", r );
                        session = createSession( r, out );
                    }
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
            session = createSession( r, out );
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
    public Optional<ISession> get(final Request r)
    {
        return getSession( r, null );
    }

    public String providerName()
    {
        return "session";
    }

}
