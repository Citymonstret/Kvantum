package com.github.intellectualsites.kvantum.implementation;

import com.codahale.metrics.Timer;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.socket.ISocketHandler;
import com.github.intellectualsites.kvantum.api.socket.SocketContext;
import com.github.intellectualsites.kvantum.implementation.error.KvantumException;
import xyz.kvantum.nanotube.NanoTube;

/**
 * Responsible for taking a {@link SocketContext} and
 * turning into a  {@link com.github.intellectualsites.kvantum.api.request.AbstractRequest}
 * and then generating a {@link Response} that is then sent back to the client
 */
final class KvantumPipeline
{

    private final NanoTube<SocketContext, WorkerContext> nanoTube;

    KvantumPipeline()
    {
        WorkerContextGenerator workerContextGenerator = new WorkerContextGenerator( Server.getInstance(),
                Server.getInstance().getProcedure().getInstance() );
        this.nanoTube = NanoTube.construct( workerContextGenerator );
        //
        // ORDERING IS VERY IMPORTANT
        //
        this.nanoTube
                .setLast( new ConnectionLogger() )
                .setLast( new SocketRequestReader() )
                .setLast( new RequestLineValidator() )
                .setLast( new RequestGenerator() )
                .setLast( new PostRequestGenerator() )
                .setLast( new SessionLoader() )
                .setLast( new RouteMatcher() )
                .setLast( new HTTPSRedirecter() )
                .setLast( new ResponseWriter() )
                .setLast( new ResponseSender() );
        this.nanoTube.setExceptionHandler( throwable ->
        {
            if ( throwable instanceof ReturnStatus )
            {
                final ReturnStatus returnStatus = (ReturnStatus) throwable;
                final Response response = new Response();
                response.getHeader().clear();
                response.getHeader().setStatus( returnStatus.getStatus() );
                response.getHeader().set( Header.HEADER_CONNECTION, "close" );
                response.getHeader().apply( returnStatus.getApplicableContext().getOutput() );
                returnStatus.getApplicableContext().flushOutput();
                if ( CoreConfig.debug )
                {
                    Message.REQUEST_SERVED_STATUS.log( returnStatus.getStatus() );
                }
            } else
            {
                new KvantumException( "Failed to handle incoming socket", throwable ).printStackTrace();
            }
        } );
    }

    void accept(final ISocketHandler socketHandler, final SocketContext socketContext)
    {
        //
        // Setup the metrics object
        //
        final Timer.Context timerContext = ServerImplementation.getImplementation()
                .getMetrics().registerRequestHandling();

        //
        // Let the pipeline do its thing
        //
        this.nanoTube.initiate( socketContext );

        //
        // Close the remote socket
        //
        socketHandler.breakSocketConnection( socketContext );

        //
        // Make sure the metric is logged
        //
        timerContext.stop();
    }

}
