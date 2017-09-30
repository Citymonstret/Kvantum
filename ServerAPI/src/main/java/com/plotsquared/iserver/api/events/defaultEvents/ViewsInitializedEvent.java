package com.plotsquared.iserver.api.events.defaultEvents;

import com.plotsquared.iserver.api.core.IntellectualServer;

public class ViewsInitializedEvent extends ServerEvent
{

    public ViewsInitializedEvent(IntellectualServer server)
    {
        super( server, "viewsInitializedEvent" );
    }

}
