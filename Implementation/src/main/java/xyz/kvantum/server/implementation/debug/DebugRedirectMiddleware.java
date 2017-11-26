package xyz.kvantum.server.implementation.debug;

import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.views.requesthandler.Middleware;
import xyz.kvantum.server.api.views.requesthandler.MiddlewareQueue;

public final class DebugRedirectMiddleware extends Middleware
{

    @Override
    public void handle(final AbstractRequest request, final MiddlewareQueue queue)
    {
        if ( !request.getQuery().getParameters().containsKey( "debug" ) )
        {
            request.internalRedirect( "" );
        } else
        {
            queue.handle( request );
        }
    }

}
