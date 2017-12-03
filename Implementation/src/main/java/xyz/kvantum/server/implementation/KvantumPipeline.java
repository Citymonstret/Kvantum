/*
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.implementation;

import com.codahale.metrics.Timer;
import lombok.AccessLevel;
import lombok.Getter;
import xyz.kvantum.nanotube.NanoTube;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.socket.ISocketHandler;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.implementation.error.KvantumException;

/**
 * Responsible for taking a {@link SocketContext} and
 * turning into a  {@link AbstractRequest}
 * and then generating a {@link Response} that is then sent back to the client
 */
final class KvantumPipeline
{

    private final NanoTube<SocketContext, WorkerContext> nanoTube;
    @Getter(AccessLevel.PACKAGE)
    private final NanoTube<WorkerContext, WorkerContext> minimalNanoTube;

    KvantumPipeline() throws Throwable
    {
        WorkerContextGenerator workerContextGenerator = new WorkerContextGenerator( Server.getInstance(),
                Server.getInstance().getProcedure().getInstance() );
        this.nanoTube = NanoTube.construct( workerContextGenerator );

        final RouteMatcher routeMatcher = new RouteMatcher();
        final HTTPSRedirecter redirecter = new HTTPSRedirecter();
        final ResponseWriter responseWriter = new ResponseWriter( this );
        final ResponseSender responseSender = new ResponseSender();
        routeMatcher.next( redirecter ).next( responseWriter ).next( responseSender );

        this.minimalNanoTube = NanoTube.construct( routeMatcher );

        //
        // ORDERING IS VERY IMPORTANT
        //
        this.nanoTube
                .setLast( new ConnectionLogger() )
                .setLast( new SocketRequestReader() )
                .setLast( new PostRequestGenerator() )
                .setLast( new ConnectionThrottle() )
                .setLast( new SessionLoader() )
                .setLast( routeMatcher );
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
