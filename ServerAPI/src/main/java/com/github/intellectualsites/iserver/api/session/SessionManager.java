/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
package com.github.intellectualsites.iserver.api.session;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.request.Cookie;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.HeaderProvider;
import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.api.util.ProviderFactory;

import java.io.BufferedOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
public final class SessionManager implements ProviderFactory<ISession>
{

    private final Map<String, ISession> sessions = new HashMap<>();
    private final ISessionCreator sessionCreator;

    public SessionManager(final ISessionCreator sessionCreator)
    {
        this.sessionCreator = sessionCreator;
    }

    private ISession createSession(final Request r, final BufferedOutputStream out)
    {
        Assert.isValid( r );

        final String sessionID = UUID.randomUUID().toString();

        r.postponedCookies.put( "session", sessionID );

        if ( CoreConfig.debug )
        {
            Message.SESSION_SET.log( "session", sessionID );
        }

        final ISession session = sessionCreator.createSession();
        session.set( "id", sessionID );
        this.sessions.put( sessionID, session );
        r.getCookies().put( "session", new Cookie( "session", sessionID ) );

        return session;
    }

    public void deleteSession(final Request r, final HeaderProvider re)
    {
        Assert.notNull( r, re );

        re.getHeader().setCookie( "session", "deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT" );
    }

    public Optional<ISession> getSession(final Request r, final BufferedOutputStream out)
    {
        Assert.isValid( r );

        ISession session = null;

        // Check cookies to see if present
        for ( Cookie cookie : r.getCookies().values() )
        {
            // If a cookie is registered
            if ( cookie.getName().equalsIgnoreCase( "session" ) )
            {
                // Cookie was found
                final String sessionID = cookie.getValue();
                // Check if session is valid
                if ( sessions.containsKey( sessionID ) )
                {
                    session = sessions.get( sessionID );
                    if ( CoreConfig.debug )
                    {
                        Message.SESSION_FOUND.log( session, sessionID, r );
                    }
                } else
                {
                    // Session isn't valid, remove old cookie
                    if ( out != null )
                    {
                        ServerImplementation.getImplementation()
                                .log( "Deleting invalid session cookie (%s) for request %s", cookie.getValue(), r );
                        session = createSession( r, out );
                        r.postponedCookies.put( "session", session.get( "id" ).toString() );
                        r.getCookies().put( "session", new Cookie( "session", session.get( "id" ).toString() ) );
                    }
                }
                break;
            }
        }

        // No session found
        if ( session == null )
        {
            session = createSession( r, out );
        }

        return Optional.ofNullable( session );
    }

    public Optional<ISession> getSession(final String sessionID)
    {
        if ( sessions.containsKey( sessionID ) )
        {
            return Optional.of( sessions.get( sessionID ) );
        }
        return Optional.empty();
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
