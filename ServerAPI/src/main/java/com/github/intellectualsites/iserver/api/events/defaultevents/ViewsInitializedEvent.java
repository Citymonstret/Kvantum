package com.github.intellectualsites.iserver.api.events.defaultevents;

import com.github.intellectualsites.iserver.api.core.IntellectualServer;

public class ViewsInitializedEvent extends ServerEvent
{

    public ViewsInitializedEvent(IntellectualServer server)
    {
        super( server, "viewsInitializedEvent" );
    }

}
