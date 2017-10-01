package com.github.intellectualsites.iserver.api.events.defaultEvents;

import com.github.intellectualsites.iserver.api.core.IntellectualServer;

public class ViewsInitializedEvent extends ServerEvent
{

    public ViewsInitializedEvent(IntellectualServer server)
    {
        super( server, "viewsInitializedEvent" );
    }

}
