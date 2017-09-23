/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.core;

import com.codahale.metrics.Timer;
import com.plotsquared.iserver.config.Message;
import com.plotsquared.iserver.logging.LogModes;
import com.plotsquared.iserver.object.AutoCloseable;
import com.plotsquared.iserver.object.*;
import com.plotsquared.iserver.object.cache.CacheApplicable;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.For;
import com.plotsquared.iserver.validation.RequestValidation;
import com.plotsquared.iserver.validation.ValidationException;
import com.plotsquared.iserver.views.RequestHandler;
import com.plotsquared.iserver.views.errors.ViewException;
import org.apache.commons.lang3.ArrayUtils;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * This is the worker that is responsible for nearly everything.
 * Feel no pressure, buddy.
 */
@SuppressWarnings("ALL")
public class Worker extends AutoCloseable
{

    private static byte[] empty = "NULL".getBytes();
    private static Queue<Worker> availableWorkers;

    private final MessageDigest messageDigestMd5;
    private final BASE64Encoder encoder;
    private final WorkerProcedure.WorkerProcedureInstance workerProcedureInstance;
    private final ReusableGzipOutputStream reusableGzipOutputStream;
    private final IntellectualServer server;
    private Request request;
    private BufferedOutputStream output;

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

        this.workerProcedureInstance = com.plotsquared.iserver.core.ServerImplementation.getImplementation()
                .getProcedure().getInstance();
        this.server = com.plotsquared.iserver.core.ServerImplementation.getImplementation();
    }

    /**
     * Setup the handler with a specified number of worker instances
     * @param n Number of worker instances (must be positive)
     */
    static void setup(final int n)
    {
        availableWorkers = new ArrayDeque<>( Assert.isPositive( n ).intValue() );

        For.upTo( n ).perform( i ->
                {
                    ServerImplementation.getImplementation().log( "Added Worker [%s]", i );
                    availableWorkers.add( new Worker() );
                }
        );

        ServerImplementation.getImplementation().log( "Availabe workers: " + availableWorkers.size() );
    }

    /**
     * Poll the worker queue until a worker is available.
     * Warning: The thread will be locked until a new worker is available
     * @return The next available worker
     */
    public static Worker getAvailableWorker()
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

    private void handle()
    {
        final RequestHandler requestHandler = server.getRouter().match( request );

        String textContent = "";
        byte[] bytes = empty;

        final Optional<Session> session = server.getSessionManager().getSession( request, output );
        if ( session.isPresent() )
        {
            request.setSession( session.get() );
        } else
        {
            request.setSession( server.getSessionManager().createSession( request, output ) );
        }

        boolean shouldCache = false;
        boolean cache = false;
        ResponseBody body;

        try
        {
            if ( !requestHandler.getValidationManager().isEmpty() )
            {
                if ( request.getQuery().getMethod() == HttpMethod.POST )
                {
                    for ( final RequestValidation<PostRequest> validator : requestHandler.getValidationManager()
                            .getValidators(
                                    RequestValidation.ValidationStage.POST_PARAMETERS ) )
                    {
                        final RequestValidation.ValidationResult result = validator.validate( request
                                .getPostRequest() );
                        if ( !result.isSuccess() )
                        {
                            throw new ValidationException( result );
                        }
                    }
                } else
                {
                    for ( final RequestValidation<Request.Query> validator : requestHandler.getValidationManager()
                            .getValidators( RequestValidation.ValidationStage.GET_PARAMETERS ) )
                    {
                        final RequestValidation.ValidationResult result = validator.validate( request.getQuery() );
                        if ( !result.isSuccess() )
                        {
                            throw new ValidationException( result );
                        }
                    }
                }
            }

            if ( CoreConfig.Cache.enabled && requestHandler instanceof CacheApplicable
                    && ( (CacheApplicable) requestHandler ).isApplicable( request ) )
            {
                cache = true;
                if ( !server.getCacheManager().hasCache( requestHandler ) )
                {
                    shouldCache = true;
                }
            }

            if ( !cache || shouldCache )
            { // Either it's a non-cached view, or there is no cache stored
                body = requestHandler.handle( request );
            } else
            { // Just read from memory
                body = server.getCacheManager().getCache( requestHandler );
            }

            boolean skip = false;
            if ( body == null )
            {
                final Object redirect = request.getMeta( "internalRedirect" );
                if ( redirect != null && redirect instanceof Request )
                {
                    this.request = (Request) redirect;
                    this.request.removeMeta( "internalRedirect" );
                    handle();
                    return;
                } else
                {
                    skip = true;
                }
            }

            if ( skip )
            {
                return;
            }

            if ( shouldCache )
            {
                server.getCacheManager().setCache( requestHandler, body );
            }

            if ( body.isText() )
            {
                textContent = body.getContent();
            } else
            {
                bytes = body.getBytes();
            }

            for ( final Map.Entry<String, String> postponedCookie : request.postponedCookies.entrySet() )
            {
                body.getHeader().setCookie( postponedCookie.getKey(), postponedCookie.getValue() );
            }

            // Start: CTYPE
            // Desc: To allow worker procedures to filter based on content type
            final Optional<String> contentType = body.getHeader().get( Header.HEADER_CONTENT_TYPE );
            if ( contentType.isPresent() )
            {
                request.addMeta( "content_type", contentType.get() );
            } else
            {
                request.addMeta( "content_type", null );
            }
            // End: CTYPE

            if ( body.isText() )
            {
                for ( final WorkerProcedure.Handler<String> handler : workerProcedureInstance.getStringHandlers() )
                {
                    textContent = handler.act( requestHandler, request, textContent );
                }
                bytes = textContent.getBytes();
            }

            if ( !workerProcedureInstance.getByteHandlers().isEmpty() )
            {
                Byte[] wrapper = ArrayUtils.toObject( bytes );
                for ( final WorkerProcedure.Handler<Byte[]> handler : workerProcedureInstance.getByteHandlers() )
                {
                    wrapper = handler.act( requestHandler, request, wrapper );
                }
                bytes = ArrayUtils.toPrimitive( wrapper );
            }
        } catch ( final Exception e )
        {
            server.log( "Error When Handling Request: %s", e.getMessage(), LogModes.MODE_ERROR );
            e.printStackTrace();

            body = new ViewException( e ).generate( request );
            bytes = body.getContent().getBytes();

            if ( CoreConfig.verbose )
            {
                e.printStackTrace();
            }
        }

        boolean gzip = false;
        if ( CoreConfig.gzip )
        {
            if ( request.getHeader( "Accept-Encoding" ).contains( "gzip" ) )
            {
                gzip = true;
                body.getHeader().set( Header.HEADER_CONTENT_ENCODING, "gzip" );
            } else
            {
                Message.CLIENT_NOT_ACCEPTING_GZIP.log( request.getHeaders() );
            }
        }

        if ( CoreConfig.contentMd5 )
        {
            body.getHeader().set( Header.HEADER_CONTENT_MD5, md5Checksum( bytes ) );
        }

        body.getHeader().apply( output );

        try
        {
            if ( gzip )
            {
                try
                {
                    bytes = compress( bytes );
                } catch ( final IOException e )
                {
                    new RuntimeException( "( GZIP ) Failed to compress the bytes" ).printStackTrace();
                }
            }
            output.write( bytes );
        } catch ( final Exception e )
        {
            new RuntimeException( "Failed to write to the client", e )
                    .printStackTrace();
        }
        try
        {
            output.flush();
        } catch ( final Exception e )
        {
            new RuntimeException( "Failed to flush to the client", e )
                    .printStackTrace();
        }

        if ( CoreConfig.debug )
        {
            server.log( "Request was served by '%s', with the type '%s'. The total length of the content was '%skB'",
                    requestHandler.getName(), body.isText() ? "text" : "bytes", (bytes.length / 1000)
            );
        }

        request.setValid( false );
    }

    /**
     * Prepares a request, then calls {@link #handle}
     * @param remote Client com.plotsquared.iserver.internal.IntellectualSocket
     */
    private void handle(final Socket remote) throws Exception
    {
        // Used for metrics
        final Timer.Context timerContext = com.plotsquared.iserver.core.ServerImplementation.getImplementation()
                .getMetrics().registerRequestHandling();
        if ( CoreConfig.verbose )
        { // Do we want to output a load of useless information?
            server.log( Message.CONNECTION_ACCEPTED, remote.getInetAddress() );
        }
        final BufferedReader input;
        { // Read the actual request
            try
            {
                input = new BufferedReader( new InputStreamReader( remote.getInputStream() ), CoreConfig.Buffer.in );
                output = new BufferedOutputStream( remote.getOutputStream(), CoreConfig.Buffer.out );

                final List<String> lines = new ArrayList<>();
                String str;
                while ( ( str = input.readLine() ) != null && !str.isEmpty() )
                {
                    lines.add( str );
                }

                request = new Request( lines, remote );

                if ( request.getQuery().getMethod() == HttpMethod.POST )
                {
                    final int cl = Integer.parseInt( request.getHeader( "Content-Length" ).substring( 1 ) );
                    request.setPostRequest( PostRequest.construct( request, cl, input ) );
                }
            } catch ( final Exception e )
            {
                e.printStackTrace();
                return;
            }
        }
        if ( /* !server.silent TODO: Replace */ true )
        {
            server.log( request.buildLog() );
        }
        handle();
        timerContext.stop();
    }

    /**
     * Accepts a remote socket and handles the incoming request,
     * also makes sure its handled and closed down successfully
     * @param remote socket to accept
     */
    public void run(final Socket remote)
    {
        if ( remote != null && !remote.isClosed() )
        {
            try
            {
                handle( remote );
            } catch ( final Exception e )
            {
                new RuntimeException( "Failed to handle incoming socket" ).printStackTrace();
            }
        }
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

        // Add the worker back to the poll
        availableWorkers.add( this );
    }

    /**
     * MD5-ify the input
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

    /**
     * Borrowed from https://github.com/oakes/Nightweb/
     */
    private static class ReusableGzipOutputStream extends DeflaterOutputStream
    {

        private static final byte[] HEADER = new byte[]{
                (byte) 0x1F, (byte) 0x8b, // magic bytes
                0x08,                   // compression format == DEFLATE
                0x00,                   // flags (NOT using CRC16, filename, etc)
                0x00, 0x00, 0x00, 0x00, // no modification time available (don't leak this!)
                0x02,                   // maximum compression
                (byte) 0xFF              // unknown creator OS (!!!)
        };
        private final ByteArrayOutputStream bufferStream;
        private final CRC32 crc32;
        private boolean headerWritten;
        private long writtenSize;
        private boolean written = false;

        private ReusableGzipOutputStream()
        {
            super( new ByteArrayOutputStream(), new Deflater( 9, true ) );
            this.crc32 = new CRC32();
            this.bufferStream = (ByteArrayOutputStream) out;
        }

        private void reset()
        {
            if ( this.written )
            {
                this.def.reset();
                this.crc32.reset();
                this.writtenSize = 0;
                this.headerWritten = false;
                this.bufferStream.reset();
                this.def.setLevel( Deflater.BEST_SPEED );
                this.written = false;
            }
        }

        private byte[] getData()
        {
            return this.bufferStream.toByteArray();
        }

        private void ensureWritten() throws IOException
        {
            if ( headerWritten )
            {
                return;
            }
            this.out.write( HEADER );
            this.headerWritten = true;
        }

        private void writeFooter() throws IOException
        {
            final long crcVal = this.crc32.getValue();
            out.write( (int) ( crcVal & 0xFF ) );
            out.write( (int) ( ( crcVal >>> 8 ) & 0xFF ) );
            out.write( (int) ( ( crcVal >>> 16 ) & 0xFF ) );
            out.write( (int) ( ( crcVal >>> 24 ) & 0xFF ) );

            final long sizeVal = this.writtenSize;
            out.write( (int) ( sizeVal & 0xFF ) );
            out.write( (int) ( ( sizeVal >>> 8 ) & 0xFF ) );
            out.write( (int) ( ( sizeVal >>> 16 ) & 0xFF ) );
            out.write( (int) ( ( sizeVal >>> 24 ) & 0xFF ) );
            out.flush();
        }

        @Override
        public void close() throws IOException
        {
            finish();
            super.close();
        }

        @Override
        public void finish() throws IOException
        {
            ensureWritten();
            super.finish();
            writeFooter();
        }

        @Override
        public void write(int b) throws IOException
        {
            this.written = true;
            this.ensureWritten();
            this.crc32.update( b );
            this.writtenSize++;
            super.write( b );
        }

        @Override
        public void write(byte[] b) throws IOException
        {
            write( b, 0, b.length );
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException
        {
            this.written = true;
            this.ensureWritten();
            this.crc32.update( buf, off, len );
            this.writtenSize += len;
            super.write( buf, off, len );
        }
    }

}
