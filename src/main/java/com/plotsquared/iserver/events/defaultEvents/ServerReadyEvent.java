package com.plotsquared.iserver.events.defaultEvents;

import com.plotsquared.iserver.core.Server;

public class ServerReadyEvent extends ServerEvent
{

    /**
     * Constructor
     *
     * @param server The server instance
     */
    public ServerReadyEvent(Server server)
    {
        super( server, "Server-Ready" );
    }

}
