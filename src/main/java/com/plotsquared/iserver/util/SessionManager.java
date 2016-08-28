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
package com.plotsquared.iserver.util;

import com.plotsquared.iserver.core.CoreConfig;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.*;
import com.plotsquared.iserver.crush.syntax.ProviderFactory;
import com.plotsquared.iserver.crush.syntax.VariableProvider;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
final public class SessionManager implements ProviderFactory<VariableProvider>
{

    private final Map<String, Session> sessions = new HashMap<>();
    private final SessionIdentifierProvider sessionIdentifierProvider;

    public SessionManager()
    {
        final String i = "" + System.nanoTime();
        this.sessionIdentifierProvider = r -> i;
    }

    public Session createSession(final Request r, final BufferedOutputStream out)
    {
        Assert.isValid( r );

        final String name = sessionIdentifierProvider.getIdentifier( r ) + "session";
        final String sessionID = UUID.randomUUID().toString();

        r.postponedCookies.put( name, sessionID );
        Server.getInstance().log( "Set session (%s=%s)", name, sessionID ); // TODO: Fix

        final Session session = new Session();
        this.sessions.put( sessionID, session );

        return session;
    }

    public void deleteSession(final Request r, final HeaderProvider re)
    {
        Assert.notNull( r, re );

        final String name = sessionIdentifierProvider.getIdentifier( r ) + "session";
        re.getHeader().setCookie( name, "deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT" );
    }

    public Optional<Session> getSession(final Request r, final OutputStream out)
    {
        Assert.isValid( r );

        Session session = null;

        final Cookie[] cookies = r.getCookies();
        final String name = sessionIdentifierProvider.getIdentifier( r ) + "session";

        final Optional<Cookie> cookie = LambdaUtil.getFirst( cookies, c -> c.getName().equalsIgnoreCase( name ) );
        if ( cookie.isPresent() )
        {
            final String sessionID = cookie.get().getValue();
            if ( sessions.containsKey( sessionID ) )
            {
                session = sessions.get( sessionID );
                if ( CoreConfig.debug )
                {
                    Server.getInstance().log( "Found session (%s=%s) for request %s", session, sessionID, r ); // TODO: Fix
                }
            } else
            {
                if ( out != null )
                {
                    r.postponedCookies.put( name, "deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT" );
                    if ( CoreConfig.debug )
                    {
                        Server.getInstance().log( "Deleting invalid session cookie (%s) for request %s", cookie.get()
                                        .getValue(),
                                r );
                    }
                }
            }
        }

        return Optional.ofNullable( session );
    }

    public VariableProvider get(final Request r)
    {
        return OptionalUtil.getOrNull( getSession( r, null ) );
    }

    public String providerName()
    {
        return "session";
    }

}
