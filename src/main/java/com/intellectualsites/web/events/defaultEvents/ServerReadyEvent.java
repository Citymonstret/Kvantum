package com.intellectualsites.web.events.defaultEvents;

import com.intellectualsites.web.core.Server;

public class ServerReadyEvent extends ServerEvent {

    /**
     * Constructor
     *
     * @param server The server instance
     */
    public ServerReadyEvent(Server server) {
        super(server, "Server-Ready");
    }

}
