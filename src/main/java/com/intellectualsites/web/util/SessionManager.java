package com.intellectualsites.web.util;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class SessionManager implements ProviderFactory<VariableProvider> {

    private Map<String, Session> sessions;
    private Server server;

    public SessionManager(Server server) {
        sessions = new HashMap<String, Session>();
        this.server = server;
    }

    public Session getSession(final Request r, PrintWriter out) {
        Cookie[] cookies = r.getCookies();
        Session session = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase("session")) {
                String sessionID = cookie.getValue();
                if (sessions.containsKey(sessionID)) {
                    session = sessions.get(sessionID);
                } else {
                    if (out != null) {
                        out.println("Set-Cookie: session=deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
                        server.log("Deleting invalid session cookie (%s)", cookie.getValue());
                    }
                }
                break;
            }
        }
        return session;
    }

    public VariableProvider get(Request r) {
        return new Session() {
            {
                set("username", "guest");
            }
        };
        //return getSession(r, null);
    }

    public String providerName() {
        return "session";
    }
}
