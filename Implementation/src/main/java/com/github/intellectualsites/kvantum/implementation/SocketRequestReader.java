package com.github.intellectualsites.kvantum.implementation;

import com.codahale.metrics.Timer;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import xyz.kvantum.nanotube.Transformer;

import java.io.BufferedOutputStream;

/**
 * Class responsible for reading HTTP requests
 */
final class SocketRequestReader extends Transformer<WorkerContext>
{

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        final Timer.Context readInput = ServerImplementation.getImplementation().getMetrics().registerReadInput();
        final BlockingSocketReader socketReader = new BlockingSocketReader( workerContext.getSocketContext(), new RequestReader() );
        workerContext.setOutput( new BufferedOutputStream( workerContext.getSocketContext().getSocket()
                .getOutputStream(), CoreConfig.Buffer.out ) );
        while ( !socketReader.isDone() )
        {
            socketReader.tick();
        }
        readInput.stop();
        workerContext.setLines( socketReader.getLines() );
        workerContext.setInputStream( socketReader.getInputStream() );
        return workerContext;
    }
}
