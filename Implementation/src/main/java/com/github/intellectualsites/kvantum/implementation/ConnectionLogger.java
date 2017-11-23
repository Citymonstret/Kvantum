package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import xyz.kvantum.nanotube.ConditionalTransformer;

/**
 * Logs {@link Message#CONNECTION_ACCEPTED} if {@link CoreConfig#verbose} is toggled
 */
final class ConnectionLogger extends ConditionalTransformer<WorkerContext>
{

    ConnectionLogger()
    {
        super( ignore -> CoreConfig.verbose );
    }

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        Message.CONNECTION_ACCEPTED.log( workerContext.getSocketContext().getAddress() );
        return workerContext;
    }

}
