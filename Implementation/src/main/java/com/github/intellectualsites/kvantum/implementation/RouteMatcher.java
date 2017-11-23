package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.response.Header;
import xyz.kvantum.nanotube.Transformer;

/**
 * Makes a {@link WorkerContext} to a {@link com.github.intellectualsites.kvantum.api.views.RequestHandler}
 */
final class RouteMatcher extends Transformer<WorkerContext>
{

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        workerContext.setRequestHandler( Server.getInstance().getRouter().match( workerContext.getRequest() ) );
        if ( workerContext.getRequestHandler() == null )
        {
            throw new ReturnStatus( Header.STATUS_NOT_FOUND, workerContext );
        }
        return workerContext;
    }
}
