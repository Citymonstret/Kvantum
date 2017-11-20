/*
 *
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
package com.github.intellectualsites.kvantum.implementation;

import com.codahale.metrics.Timer;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.core.WorkerProcedure;
import com.github.intellectualsites.kvantum.api.exceptions.ProtocolNotSupportedException;
import com.github.intellectualsites.kvantum.api.exceptions.QueryException;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.request.HttpMethod;
import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.request.post.*;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.response.ResponseBody;
import com.github.intellectualsites.kvantum.api.socket.SocketContext;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.implementation.error.KvantumException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * This is the worker that is responsible for nearly everything.
 * Feel no pressure, buddy.
 */
final class Worker
{

    static AbstractPool<GzipHandler> gzipHandlerPool;
    static AbstractPool<Md5Handler> md5HandlerPool;

    private final WorkerProcedure.WorkerProcedureInstance workerProcedureInstance;
    private final Kvantum server;
    private final SocketHandler socketHandler;

    Worker(final SocketHandler socketHandler)
    {
        this.workerProcedureInstance = ServerImplementation.getImplementation()
                .getProcedure().getInstance();
        this.server = ServerImplementation.getImplementation();
        this.socketHandler = socketHandler;
    }

    void sendToClient(final WorkerContext workerContext, final ResponseBody body, byte[] bytes)
    {
        workerContext.determineGzipStatus();

        if ( CoreConfig.contentMd5 )
        {
            final Md5Handler md5Handler = md5HandlerPool.getNullable();
            body.getHeader().set( Header.HEADER_CONTENT_MD5, md5Handler.generateChecksum( bytes ) );
            md5HandlerPool.add( md5Handler );
        }

        if ( workerContext.isGzip() )
        {
            final Timer.Context context = ServerImplementation.getImplementation().getMetrics().registerCompression();
            try
            {
                final GzipHandler gzipHandler = gzipHandlerPool.getNullable();
                bytes = gzipHandler.compress( bytes );
                if ( body.getHeader().hasHeader( Header.HEADER_CONTENT_LENGTH ) )
                {
                    body.getHeader().set( Header.HEADER_CONTENT_LENGTH, "" + bytes.length );
                }
                gzipHandlerPool.add( gzipHandler );
            } catch ( final IOException e )
            {
                new KvantumException( "( GZIP ) Failed to compress the bytes" ).printStackTrace();
            }
            context.stop();
        }

        //
        // Send the header to the client
        //
        body.getHeader().apply( workerContext.getOutput() );

        try
        {
            workerContext.getOutput().write( bytes );
        } catch ( final Exception e )
        {
            new KvantumException( "Failed to write to the client", e )
                    .printStackTrace();
        }

        //
        // Make sure everything is written
        //
        workerContext.flushOutput();

        //
        // Invalidate request to make sure that it isn't handled anywhere else, again (wouldn't work)
        //
        workerContext.getRequest().setValid( false );
    }

    /**
     * Prepares a request
     * @param remote Client
     * @return boolean success status
     */
    private boolean prepare(final WorkerContext workerContext, final SocketContext remote) throws Exception
    {
        if ( CoreConfig.verbose )
        {
            this.server.log( Message.CONNECTION_ACCEPTED, remote.getAddress() );
        }

        final Timer.Context readInput = ServerImplementation.getImplementation().getMetrics().registerReadInput();

        final BlockingSocketReader socketReader = new BlockingSocketReader( remote, new RequestReader() );
        workerContext.setOutput( new BufferedOutputStream( remote.getSocket()
                .getOutputStream(), CoreConfig.Buffer.out ) );

        //
        // Read the request
        //
        while ( !socketReader.isDone() )
        {
            socketReader.tick();
        }

        final Collection<String> lines = socketReader.getLines();

        readInput.stop();

        //
        // Make sure that the client cannot send an extreme number of lines in their request to bypass
        // the size limit. This is to prevent DOS attacks, and also... who sends 100+ headers?
        //
        if ( lines.size() > CoreConfig.Limits.limitRequestLines )
        {
            return handleSendStatusOnly( workerContext, Header.STATUS_PAYLOAD_TOO_LARGE );
        }

        //
        // Generate a new request
        //
        try
        {
            final Timer.Context metricContext = ServerImplementation.getImplementation().getMetrics()
                    .registerRequestPreparation();
            workerContext.setRequest( new Request( lines, remote ) );
            metricContext.stop();
        } catch ( final ProtocolNotSupportedException ex )
        {
            return handleSendStatusOnly( workerContext, Header.STATUS_HTTP_VERSION_NOT_SUPPORTED );
        } catch ( final QueryException ex )
        {
            Logger.error( "Failed to read query (%s)", ex.getMessage() );
            return handleSendStatusOnly( workerContext, Header.STATUS_BAD_REQUEST );
        } catch ( final Exception ex )
        {
            ex.printStackTrace();
            return handleSendStatusOnly( workerContext, Header.STATUS_BAD_REQUEST );
        }

        workerContext.getRequest().setInputReader( new BufferedReader(
                new InputStreamReader( socketReader.getInputStream() ) ) );

        //
        // If the client sent a post request, then make sure to the read the request field
        //
        if ( workerContext.getRequest().getQuery().getMethod() == HttpMethod.POST )
        {
            final AbstractRequest request = workerContext.getRequest();
            final String contentType = request.getHeader( "Content-Type" );

            boolean isFormURLEncoded;
            boolean isJSON = false;

            if ( ( isFormURLEncoded = contentType.equalsIgnoreCase( "application/x-www-form-urlencoded" ) ) ||
                    ( isJSON = EntityType.JSON.getContentType().equals( contentType ) ) )
            {
                final int contentLength;
                try
                {
                    contentLength = Integer.parseInt( request.getHeader( "Content-Length" ) );
                } catch ( final Exception ignored )
                {
                    return handleSendStatusOnly( workerContext, Header.STATUS_BAD_REQUEST );
                }
                if ( contentLength >= CoreConfig.Limits.limitPostBasicSize )
                {
                    if ( CoreConfig.debug )
                    {
                        Logger.debug( "Supplied post body size too large (%s > %s)", contentLength,
                                CoreConfig.Limits.limitPostBasicSize );
                    }
                    return handleSendStatusOnly( workerContext, Header.STATUS_ENTITY_TOO_LARGE );
                }
                try
                {
                    final char[] characters = new char[ contentLength ];
                    Assert.equals( request.getInputReader().read( characters ), contentLength );
                    if ( isFormURLEncoded )
                    {
                        request.setPostRequest( new UrlEncodedPostRequest( request, new String( characters ) ) );
                    } else
                    {
                        request.setPostRequest( new JsonPostRequest( request, new String( characters ) ) );
                    }
                } catch ( final Exception e )
                {
                    Logger.warn( "Failed to read url encoded post request (Request: %s): %s",
                            request, e.getMessage() );
                }
            } else if ( contentType.startsWith( "multipart" ) )
            {
                request.setPostRequest( new MultipartPostRequest( request, "" ) );
            } else
            {
                Logger.warn( "Request provided unknown post request type (Request: %s): %s", request,
                        contentType );
                request.setPostRequest( new DummyPostRequest( request, "" ) );
            }

            request.getPostRequest().get().forEach( (k, v) -> Logger.debug( "Key: %s | Value: %s", k, v ) );
        }

        //
        // Log the request
        //
        if ( !server.isSilent() )
        {
            server.log( workerContext.getRequest().buildLog() );
        }

        return true;
    }

    /**
     * This method sends only a HTTP status to the client
     * See {@link Header} for status constants
     * <p>
     * This method will generate a new response and then flush the output (see {@link WorkerContext#flushOutput()}
     *
     * @param status Status code
     * @return false
     */
    boolean handleSendStatusOnly(final WorkerContext workerContext, final String status)
    {
        Response response = new Response();
        response.getHeader().clear();
        response.getHeader().setStatus( status );
        response.getHeader().set( Header.HEADER_CONNECTION, "close" );
        response.getHeader().apply( workerContext.getOutput() );
        workerContext.flushOutput();

        if ( CoreConfig.debug )
        {
            Message.REQUEST_SERVED_STATUS.log( status );
        }

        return false;
    }

    /**
     * Accepts a remote socket and handles the incoming request,
     * also makes sure its handled and closed down successfully.
     *
     * This method passes the request to {@link #prepare(WorkerContext, SocketContext)}
     *
     * @param remote socket to accept
     */
    void run(final SocketContext remote)
    {
        //
        // Setup the metrics object
        //
        final Timer.Context timerContext = ServerImplementation.getImplementation()
                .getMetrics().registerRequestHandling();

        final WorkerContext workerContext = new WorkerContext( server, workerProcedureInstance );

        //
        // Handle the remote socket
        //
        if ( remote.isActive() )
        {
            try
            {
                final Timer.Context workerPrepare = ServerImplementation.getImplementation().getMetrics()
                        .registerWorkerPrepare();
                final boolean prepared = this.prepare( workerContext, remote );
                workerPrepare.stop();
                if ( prepared )
                {
                    final Timer.Context contextTimerContext = ServerImplementation.getImplementation().getMetrics()
                            .registerWorkerContextHandling();
                    workerContext.handle( this );
                    contextTimerContext.stop();
                }
            } catch ( final Exception e )
            {
                new KvantumException( "Failed to handle incoming socket", e ).printStackTrace();
            }
        }

        //
        // Close the remote socket
        //
        this.socketHandler.breakSocketConnection( remote );

        //
        // Make sure the metric is logged
        //
        timerContext.stop();
    }
}
