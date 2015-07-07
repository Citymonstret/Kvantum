package com.intellectualsites.web.events.defaultEvents;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.events.Event;

/**
 * An event wrapper for events that involved the server
 *
 * @author Citymonstret
 */
public abstract class ServerEvent extends Event {

    private final Server server;

    /**
     * Constructor
     *
     * @param server The server instance
     * @param name The event identifier
     */
    public ServerEvent(final Server server, final String name) {
        super("is::server::" + name);
        this.server = server;
    }

    /**
     * Get the server instance
     *
     * @return server instance
     */
    public final Server getServer() {
        return this.server;
    }
}
