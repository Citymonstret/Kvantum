package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.util.ProtocolType;
import com.github.intellectualsites.kvantum.api.views.requesthandler.HTTPSRedirectHandler;
import xyz.kvantum.nanotube.ConditionalTransformer;

/**
 * Redirects requests to HTTPS, if this is desired
 */
final class HTTPSRedirecter extends ConditionalTransformer<WorkerContext>
{

    HTTPSRedirecter()
    {
        super( workerContext -> workerContext.getRequest().getProtocolType() != ProtocolType.HTTPS && workerContext
                .getRequestHandler().forceHTTPS() );
    }

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        if ( CoreConfig.debug )
        {
            Logger.debug( "Redirecting request [%s] to HTTPS version of [%s]", workerContext.getRequest(),
                    workerContext.getRequestHandler() );
        }
        if ( !CoreConfig.SSL.enable )
        {
            Logger.error( "RequestHandler (%s) forces HTTPS but SSL runner not enabled!" );
            throw new ReturnStatus( Header.STATUS_INTERNAL_ERROR, workerContext );
        }
        workerContext.setRequestHandler( HTTPSRedirectHandler.getInstance() );
        return workerContext;
    }
}
