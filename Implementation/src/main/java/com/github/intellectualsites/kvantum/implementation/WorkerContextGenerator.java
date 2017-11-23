package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.core.WorkerProcedure;
import com.github.intellectualsites.kvantum.api.socket.SocketContext;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.nanotube.Generator;

/**
 * Class responsible for creating {@link WorkerContext} instances
 * from incoming {@link SocketContext} instances
 */
@RequiredArgsConstructor
final class WorkerContextGenerator extends Generator<SocketContext, WorkerContext>
{

    private final Kvantum server;
    private final WorkerProcedure.WorkerProcedureInstance workerProcedureInstance;

    @Override
    protected WorkerContext handle(final SocketContext socketContext) throws Throwable
    {
        final WorkerContext workerContext = new WorkerContext( server, workerProcedureInstance );
        workerContext.setSocketContext( socketContext );
        return workerContext;
    }

}
