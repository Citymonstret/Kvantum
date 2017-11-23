package com.github.intellectualsites.kvantum.implementation;

import com.codahale.metrics.Timer;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.exceptions.ProtocolNotSupportedException;
import com.github.intellectualsites.kvantum.api.exceptions.QueryException;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.response.Header;
import xyz.kvantum.nanotube.Transformer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Generates a {@link com.github.intellectualsites.kvantum.api.request.AbstractRequest}
 * from a {@link WorkerContext}
 */
final class RequestGenerator extends Transformer<WorkerContext>
{

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        try
        {
            final Timer.Context metricContext = ServerImplementation.getImplementation().getMetrics()
                    .registerRequestPreparation();
            workerContext.setRequest( new Request( workerContext.getLines(), workerContext.getSocketContext() ) );
            metricContext.stop();
        } catch ( final ProtocolNotSupportedException ex )
        {
            throw new ReturnStatus( Header.STATUS_HTTP_VERSION_NOT_SUPPORTED, workerContext );
        } catch ( final QueryException ex )
        {
            Logger.error( "Failed to read query (%s)", ex.getMessage() );
            throw new ReturnStatus( Header.STATUS_BAD_REQUEST, workerContext );
        } catch ( final Exception ex )
        {
            ex.printStackTrace();
            throw new ReturnStatus( Header.STATUS_BAD_REQUEST, workerContext );
        }
        workerContext.getRequest().setInputReader( new BufferedReader(
                new InputStreamReader( workerContext.getInputStream() ) ) );
        workerContext.getRequest().setOutputStream( workerContext.getOutput() );
        return workerContext;
    }
}
