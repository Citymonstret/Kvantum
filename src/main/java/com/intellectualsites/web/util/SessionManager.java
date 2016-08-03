//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualsites.web.util;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.syntax.VariableProvider;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class SessionManager implements ProviderFactory<VariableProvider> {

    private final Map<String, Session> sessions;
    private final Server server;
    private final SessionIdentifierProvider sessionIdentifierProvider;

    public SessionManager(Server server) {
        sessions = new HashMap<>();
        this.server = server;

        final String i = "" + System.nanoTime();
        this.sessionIdentifierProvider = r -> i;
    }

    public Session createSession(Request r, BufferedOutputStream out) {
        String name = sessionIdentifierProvider.getIdentifier(r) + "session";
        String sessionID = UUID.randomUUID().toString();
        r.postponedCookies.put(name, sessionID);
        server.log("Set session (%s=%s)", name, sessionID);
        Session session = new Session();
        this.sessions.put(sessionID, session);
        return session;
    }

    public void deleteSession(final Request r, final HeaderProvider re) {
        String name = sessionIdentifierProvider.getIdentifier(r) + "session";
        re.getHeader().setCookie(name, "deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
        server.log("Deleted cookie");
    }

    public Session getSession(final Request r, OutputStream out) {
        Cookie[] cookies = r.getCookies();
        Session session = null;

        String name = sessionIdentifierProvider.getIdentifier(r) + "session";

        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(name)) {
                String sessionID = cookie.getValue();
                if (sessions.containsKey(sessionID)) {
                    session = sessions.get(sessionID);
                    server.log("Found session (%s=%s)", session, sessionID);
                } else {
                    if (out != null) {
                        r.postponedCookies.put(name, "deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
                        server.log("Deleting invalid session cookie (%s)", cookie.getValue());
                    }
                }
                break;
            }
        }

        return session;
    }

    public VariableProvider get(Request r) {
        return getSession(r, null);
    }

    public String providerName() {
        return "session";
    }

}
