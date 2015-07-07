package com.intellectualsites.web.events.defaultEvents;

import com.intellectualsites.web.core.Server;

/**
 * Called when the server shuts down
 *
 * @author Citymonstret
 */
public class ShutdownEvent extends ServerEvent {

    public ShutdownEvent(Server server) {
        super(server, "shutdown");
    }
}
