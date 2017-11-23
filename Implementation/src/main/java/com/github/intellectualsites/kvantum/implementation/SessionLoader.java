package com.github.intellectualsites.kvantum.implementation;

import com.codahale.metrics.Timer;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import xyz.kvantum.nanotube.ConditionalTransformer;

/**
 * Loads the session, if {@link CoreConfig.Sessions#autoLoad} is toggled
 */
final class SessionLoader extends ConditionalTransformer<WorkerContext>
{

    SessionLoader()
    {
        super( ignore -> CoreConfig.Sessions.autoLoad );
    }

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        final Timer.Context context = ServerImplementation.getImplementation().getMetrics()
                .registerSessionPreparation();
        workerContext.getRequest().requestSession();
        context.stop();
        return workerContext;
    }
}
