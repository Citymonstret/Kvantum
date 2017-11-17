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
import com.github.intellectualsites.kvantum.api.util.AutoCloseable;
import com.github.intellectualsites.kvantum.api.util.Provider;
import com.github.intellectualsites.kvantum.implementation.error.KvantumException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This is the worker that is responsible for nearly everything.
 * Feel no pressure, buddy.
 */
final class Worker extends AutoCloseable
{

    private final WorkerProcedure.WorkerProcedureInstance workerProcedureInstance;
    private final GzipHandler gzipHandler;
    private final Md5Handler md5Handler;
    private final Kvantum server;

    private WorkerContext workerContext;

    Worker()
    {
        this.md5Handler = createIf( Md5Handler::new, CoreConfig.contentMd5 );
        this.gzipHandler = createIf( GzipHandler::new, CoreConfig.gzip );

        this.workerProcedureInstance = ServerImplementation.getImplementation()
                .getProcedure().getInstance();
        this.server = ServerImplementation.getImplementation();
    }

    private <T> T createIf(final Provider<T> provider, final boolean condition)
    {
        if ( condition )
        {
            return provider.provide();
        }
        return null;
    }

    @Override
    protected void handleClose()
    {
    }

    void sendToClient(final ResponseBody body, byte[] bytes)
    {
        workerContext.determineGzipStatus();

        if ( CoreConfig.contentMd5 )
        {
            body.getHeader().set( Header.HEADER_CONTENT_MD5, md5Handler.generateChecksum( bytes ) );
        }

        if ( workerContext.isGzip() )
        {
            final Timer.Context context = ServerImplementation.getImplementation().getMetrics().registerCompression();
            try
            {
                bytes = gzipHandler.compress( bytes );
                if ( body.getHeader().hasHeader( Header.HEADER_CONTENT_LENGTH ) )
                {
                    body.getHeader().set( Header.HEADER_CONTENT_LENGTH, "" + bytes.length );
                }
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
    private boolean prepare(final SocketContext remote) throws Exception
    {
        if ( CoreConfig.verbose )
        {
            this.server.log( Message.CONNECTION_ACCEPTED, remote.getAddress() );
        }

        final Timer.Context readInput = ServerImplementation.getImplementation().getMetrics().registerReadInput();

        final BufferedReader input = new BufferedReader( new InputStreamReader( remote.getSocket().getInputStream() ),
                CoreConfig.Buffer.in );
        this.workerContext.setOutput( new BufferedOutputStream( remote.getSocket()
                .getOutputStream(), CoreConfig.Buffer.out ) );

        //
        // Read the request
        //
        final Deque<String> lines = new ArrayDeque<>( CoreConfig.Buffer.lineQueInitialization );
        String str;
        while ( ( str = input.readLine() ) != null && !str.isEmpty() )
        {
            //
            // Make sure that a request line (in case of headers: both the key and the value
            // doesn't exceed the limit. This is to prevent the client from sending enormous requests
            //
            // The string length is multiplied by two, to get the number of bytes in the string
            //
            if ( ( str.length() * 2 ) > CoreConfig.Limits.limitRequestLineSize )
            {
                return handleSendStatusOnly( Header.STATUS_PAYLOAD_TOO_LARGE );
            }

            lines.add( str );
        }


        readInput.stop();

        //
        // Make sure that the client cannot send an extreme number of lines in their request to bypass
        // the size limit. This is to prevent DOS attacks, and also... who sends 100+ headers?
        //
        if ( lines.size() > CoreConfig.Limits.limitRequestLines )
        {
            return handleSendStatusOnly( Header.STATUS_PAYLOAD_TOO_LARGE );
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
            return handleSendStatusOnly( Header.STATUS_HTTP_VERSION_NOT_SUPPORTED );
        } catch ( final QueryException ex )
        {
            Logger.error( "Failed to read query (%s)", ex.getMessage() );
            return handleSendStatusOnly( Header.STATUS_BAD_REQUEST );
        } catch ( final Exception ex )
        {
            ex.printStackTrace();
            return handleSendStatusOnly( Header.STATUS_BAD_REQUEST );
        }

        this.workerContext.getRequest().setInputReader( input );

        //
        // If the client sent a post request, then make sure to the read the request field
        //
        if ( workerContext.getRequest().getQuery().getMethod() == HttpMethod.POST )
        {
            final AbstractRequest request = workerContext.getRequest();
            final String contentType = request.getHeader( "Content-Type" );

            boolean isFormURLEncoded = false;
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
                    return handleSendStatusOnly( Header.STATUS_BAD_REQUEST );
                }
                if ( contentLength >= CoreConfig.Limits.limitPostBasicSize )
                {
                    if ( CoreConfig.debug )
                    {
                        Logger.debug( "Supplied post body size too large (%s > %s)", contentLength,
                                CoreConfig.Limits.limitPostBasicSize );
                    }
                    return handleSendStatusOnly( Header.STATUS_ENTITY_TOO_LARGE );
                }
                try
                {
                    final char[] characters = new char[ contentLength ];
                    Assert.equals( input.read( characters ), contentLength );
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
    boolean handleSendStatusOnly(final String status)
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
     * This method passes the request to {@link #prepare(SocketContext)}
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

        this.workerContext = new WorkerContext( server, workerProcedureInstance );

        //
        // Handle the remote socket
        //
        if ( remote.isActive() )
        {
            try
            {
                final Timer.Context workerPrepare = ServerImplementation.getImplementation().getMetrics()
                        .registerWorkerPrepare();
                final boolean prepared = this.prepare( remote );
                workerPrepare.stop();
                if ( prepared )
                {
                    final Timer.Context contextTimerContext = ServerImplementation.getImplementation().getMetrics()
                            .registerWorkerContextHandling();
                    this.workerContext.handle( this );
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
        remote.close();

        //
        // Make sure the metric is logged
        //
        timerContext.stop();

        //
        // Add the worker back to the pool
        //
        WorkerPool.addWorker( this );
    }
}
