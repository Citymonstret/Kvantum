package com.intellectualsites.web.events.defaultEvents;

import com.intellectualsites.web.core.Server;

/**
 * Created 2015-05-03 for IntellectualServer
 *
 * @author Citymonstret
 */
public class StartupEvent extends ServerEvent {

    public StartupEvent(final Server server) {
        super(server, "startup");
    }

}
