package com.intellectualsites.web.events.defaultEvents;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.events.Event;

/**
 * Created 2015-05-03 for IntellectualServer
 *
 * @author Citymonstret
 */
public abstract class ServerEvent extends Event {

    private final Server server;

    public ServerEvent(final Server server, final String name) {
        super("is::server::" + name);
        this.server = server;
    }

    public final Server getServer() {
        return this.server;
    }

}
