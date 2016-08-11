package com.plotsquared.iserver.views.requesthandler;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.logging.LogModes;
import com.plotsquared.iserver.object.Request;

public final class DebugMiddleware extends Middleware
{

    @Override
    public void handle(Request request, MiddlewareQueue queue)
    {
        if ( request.getMeta( "zmetakey" ) != null )
        {
            queue.handle( request );
        } else
        {
            Server.getInstance().log( "DebugMiddleware :> Request doesn't have a stored 'zmetakey', dropping!",
                    LogModes.MODE_WARNING );
        }
    }

}
