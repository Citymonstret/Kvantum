/*
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.implementation;

import com.codahale.metrics.Timer;
import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.core.WorkerProcedure;
import com.github.intellectualsites.iserver.api.exceptions.ProtocolNotSupportedException;
import com.github.intellectualsites.iserver.api.exceptions.QueryException;
import com.github.intellectualsites.iserver.api.logging.Logger;
import com.github.intellectualsites.iserver.api.request.HttpMethod;
import com.github.intellectualsites.iserver.api.request.PostRequest;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Header;
import com.github.intellectualsites.iserver.api.response.Response;
import com.github.intellectualsites.iserver.api.response.ResponseBody;
import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.api.util.AutoCloseable;
import com.github.intellectualsites.iserver.api.util.LambdaUtil;
import com.github.intellectualsites.iserver.implementation.error.IntellectualServerException;
import sun.misc.BASE64Encoder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * This is the worker that is responsible for nearly everything.
 * Feel no pressure, buddy.
 */
final class Worker extends AutoCloseable
{

    private static Queue<Worker> availableWorkers;

    private final MessageDigest messageDigestMd5;
    private final BASE64Encoder encoder;
    private final WorkerProcedure.WorkerProcedureInstance workerProcedureInstance;
    private final ReusableGzipOutputStream reusableGzipOutputStream;
    private final IntellectualServer server;

    private WorkerContext workerContext;

    private Worker()
    {
        if ( CoreConfig.contentMd5 )
        {
            MessageDigest temporary = null;
            try
            {
                temporary = MessageDigest.getInstance( "MD5" );
            } catch ( final NoSuchAlgorithmException e )
            {
                Message.MD5_DIGEST_NOT_FOUND.log( e.getMessage() );
            }
            messageDigestMd5 = temporary;
            encoder = new BASE64Encoder();
        } else
        {
            messageDigestMd5 = null;
            encoder = null;
        }

        if ( CoreConfig.gzip )
        {
            this.reusableGzipOutputStream = new ReusableGzipOutputStream();
        } else
        {
            this.reusableGzipOutputStream = null;
        }

        this.workerProcedureInstance = ServerImplementation.getImplementation()
                .getProcedure().getInstance();
        this.server = ServerImplementation.getImplementation();
    }

    /**
     * Setup the handler with a specified number of worker instances
     *
     * @param n Number of worker instances (must be positive)
     */
    static void setup(final int n)
    {
        availableWorkers = new ArrayDeque<>( Assert.isPositive( n ).intValue() );
        LambdaUtil.collectionAssign( () -> availableWorkers, Worker::new, n );
        Message.WORKER_AVAILABLE.log( availableWorkers.size() );
    }

    /**
     * Poll the worker queue until a worker is available.
     * Warning: The thread will be locked until a new worker is available
     *
     * @return The next available worker
     */
    static Worker getAvailableWorker()
    {
        Worker worker = Assert.notNull( availableWorkers ).poll();
        while ( worker == null )
        {
            worker = availableWorkers.poll();
        }
        return worker;
    }

    @Override
    protected void handleClose()
    {
        if ( CoreConfig.gzip )
        {
            try
            {
                this.reusableGzipOutputStream.close();
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Compress bytes using gzip
     *
     * @param data Bytes to compress
     * @return GZIP compressed data
     * @throws IOException If compression fails
     */
    private byte[] compress(final byte[] data) throws IOException
    {
        Assert.notNull( data );

        reusableGzipOutputStream.reset();
        reusableGzipOutputStream.write( data );
        reusableGzipOutputStream.finish();
        reusableGzipOutputStream.flush();

        final byte[] compressed = reusableGzipOutputStream.getData();

        Assert.equals( compressed != null && compressed.length > 0, true, "Failed to compress data" );

        return compressed;
    }

    void sendToClient(final ResponseBody body, byte[] bytes)
    {
        workerContext.determineGzipStatus();

        if ( CoreConfig.contentMd5 )
        {
            body.getHeader().set( Header.HEADER_CONTENT_MD5, md5Checksum( bytes ) );
        }

        if ( workerContext.isGzip() )
        {
            try
            {
                bytes = compress( bytes );
                if ( body.getHeader().hasHeader( Header.HEADER_CONTENT_LENGTH ) )
                {
                    body.getHeader().set( Header.HEADER_CONTENT_LENGTH, "" + bytes.length );
                }
            } catch ( final IOException e )
            {
                new IntellectualServerException( "( GZIP ) Failed to compress the bytes" ).printStackTrace();
            }
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
            new IntellectualServerException( "Failed to write to the client", e )
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
    private boolean prepare(final Socket remote) throws Exception
    {
        if ( CoreConfig.verbose )
        {
            this.server.log( Message.CONNECTION_ACCEPTED, remote.getInetAddress() );
        }

        final BufferedReader input = new BufferedReader( new InputStreamReader( remote.getInputStream() ), CoreConfig.Buffer.in );
        this.workerContext.setOutput( new BufferedOutputStream( remote.getOutputStream(), CoreConfig.Buffer.out ) );

        //
        // Read the request
        //
        final List<String> lines = new ArrayList<>();
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
            workerContext.setRequest( new Request( lines, remote ) );
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

        //
        // If the client sent a post request, then make sure to the read the request field
        //
        if ( workerContext.getRequest().getQuery().getMethod() == HttpMethod.POST )
        {
            final Request request = workerContext.getRequest();
            final int cl = Integer.parseInt( workerContext.getRequest().getHeader( "Content-Length" ) );
            request.setPostRequest( PostRequest.construct( workerContext.getRequest(), cl, input ) );
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
     * This method passes the request to {@link #prepare(Socket)}
     *
     * @param remote socket to accept
     */
    void run(final Socket remote)
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
        if ( remote != null && !remote.isClosed() )
        {
            try
            {
                if ( this.prepare( remote ) )
                {
                    this.workerContext.handle( this );
                }
            } catch ( final Exception e )
            {
                new IntellectualServerException( "Failed to handle incoming socket", e ).printStackTrace();
            }
        }

        //
        // Close the remote socket
        //
        this.closeRemote( remote );

        //
        // Make sure the metric is logged
        //
        timerContext.stop();

        //
        // Add the worker back to the pool
        //
        availableWorkers.add( this );
    }

    /**
     * Close the remote socket, prints any exceptions
     *
     * @param remote Remote socket
     */
    private void closeRemote(final Socket remote)
    {
        if ( remote != null && !remote.isClosed() )
        {
            try
            {
                remote.close();
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * MD5-ify the input
     *
     * @param input Input text to be digested
     * @return md5-ified digested text
     */
    private String md5Checksum(final byte[] input)
    {
        Assert.notNull( input );

        // Make sure that the buffer is clean
        messageDigestMd5.reset();
        // Update the digest with the current input
        messageDigestMd5.update( input );
        // Now encode it, yay
        return encoder.encode( messageDigestMd5.digest() );
    }

}
