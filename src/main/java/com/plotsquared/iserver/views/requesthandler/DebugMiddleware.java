package com.plotsquared.iserver.views.requesthandler;

import com.plotsquared.iserver.object.Request;

public final class DebugMiddleware extends Middleware
{

    @Override
    public void handle(final Request request, final MiddlewareQueue queue)
    {
        request.useAlternateOutcome( "debug" );
        queue.handle( request );
    }

}
