package com.intellectualsites.web.events.defaultEvents;

import com.intellectualsites.web.core.Server;

/**
 * Called when the servers starts up
 *
 * @author Citymonstret
 */
public class StartupEvent extends ServerEvent {

    public StartupEvent(final Server server) {
        super(server, "startup");
    }

}
