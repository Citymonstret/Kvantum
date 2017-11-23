package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.response.Header;
import xyz.kvantum.nanotube.Transformer;

/**
 * Makes sure there aren't too many lines in the request
 */
final class RequestLineValidator extends Transformer<WorkerContext>
{

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        if ( workerContext.getLines().size() > CoreConfig.Limits.limitRequestLines )
        {
            throw new ReturnStatus( Header.STATUS_PAYLOAD_TOO_LARGE, workerContext );
        }
        return workerContext;
    }
}
