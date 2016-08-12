//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2016 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.core;

import com.plotsquared.iserver.config.Message;
import com.plotsquared.iserver.object.*;
import com.plotsquared.iserver.object.cache.CacheApplicable;
import com.plotsquared.iserver.object.syntax.IgnoreSyntax;
import com.plotsquared.iserver.object.syntax.ProviderFactory;
import com.plotsquared.iserver.object.syntax.Syntax;
import com.plotsquared.iserver.thread.ThreadManager;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.views.RequestHandler;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

class Worker
{

    private static byte[] empty = "NULL".getBytes();
    private static int idPool = 0;

    private final int id;
    private final MessageDigest messageDigestMd5;
    private final BASE64Encoder encoder;

    Worker()
    {
        this.id = idPool++;
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
    }

    synchronized void start()
    {
        Server.getInstance().log( "Started thread: " + id );
        final Server server = Server.getInstance();
        ThreadManager.createThread( () ->
        {
            if ( !server.queue.isEmpty() )
            {
                Socket current = server.queue.poll();
                run( current, server );
            }
        } );
    }

    private void handle(final Request request, final Server server, final BufferedOutputStream output)
    {
        final RequestHandler requestHandler = server.requestManager.match( request );

        String textContent = "";
        byte[] bytes = empty;

        final Session session = server.sessionManager.getSession( request, output );
        if ( session != null )
        {
            request.setSession( session );
        } else
        {
            request.setSession( server.sessionManager.createSession( request, output ) );
        }

        boolean shouldCache = false;
        boolean cache = false;
        final ResponseBody body;

        if ( server.enableCaching && requestHandler instanceof CacheApplicable
                && ( (CacheApplicable) requestHandler ).isApplicable( request ) )
        {
            cache = true;
            if ( !server.cacheManager.hasCache( requestHandler ) )
            {
                shouldCache = true;
            }
        }

        if ( !cache || shouldCache )
        { // Either it's a non-cached view, or there is no cache stored
            body = requestHandler.handle( request );
        } else
        { // Just read from memory
            body = server.cacheManager.getCache( requestHandler );
        }

        boolean skip = false;
        if ( body == null )
        {
            final Object redirect = request.getMeta( "internalRedirect" );
            if ( redirect != null && redirect instanceof Request )
            {
                final Request newRequest = (Request) redirect;
                newRequest.removeMeta( "internalRedirect" );
                handle( newRequest, server, output );
                return;
            } else
            {
                skip = true;
            }
        }

        if ( !skip )
        {
            if ( shouldCache )
            {
                server.cacheManager.setCache( requestHandler, body );
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

            if ( body.isText() )
            {
                // Make sure to not use Crush when
                // told not to
                if ( !( requestHandler instanceof IgnoreSyntax ) )
                {
                    // Provider factories are fun, and so is the
                    // global map. But we also need the view
                    // specific ones!
                    final Map<String, ProviderFactory> factories = new HashMap<>();
                    for ( final ProviderFactory factory : server.providers )
                    {
                        factories.put( factory.providerName().toLowerCase(), factory );
                    }
                    // Now make use of the view specific ProviderFactory
                    final ProviderFactory z = requestHandler.getFactory( request );
                    if ( z != null )
                    {
                        factories.put( z.providerName().toLowerCase(), z );
                    }
                    factories.put( "request", request );
                    // This is how the crush engine works.
                    // Quite simple, yet powerful!
                    for ( final Syntax syntax : server.syntaxes )
                    {
                        if ( syntax.matches( textContent ) )
                        {
                            textContent = syntax.handle( textContent, request, factories );
                        }
                    }
                }
                // Now, finally, let's get the bytes.
                bytes = textContent.getBytes();
            }

            boolean gzip = false;
            if ( CoreConfig.gzip ) {
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
        }

        if ( !server.silent && !skip )
        {
            server.log( "Request was served by '%s', with the type '%s'. The total length of the content was '%s'",
                    requestHandler.getName(), body.isText() ? "text" : "bytes", bytes.length
            );
        }

        request.setValid( false );
    }

    private void run(final Socket remote, final Server server)
    {
        if ( remote == null || remote.isClosed() )
        {
            return; // TODO: Why?
        }

        Assert.notNull( server );

        if ( CoreConfig.verbose )
        {         // Do we want to output a load of useless information?
            server.log( Message.CONNECTION_ACCEPTED, remote.getInetAddress() );
        }

        final Request request;
        final BufferedOutputStream output;
        final BufferedReader input;

        { // Read the actual request
            final StringBuilder rRaw = new StringBuilder();
            try
            {
                input = new BufferedReader( new InputStreamReader( remote.getInputStream() ), server.bufferIn );
                output = new BufferedOutputStream( remote.getOutputStream(), server.bufferOut );
                String str;
                while ( ( str = input.readLine() ) != null && !str.equals( "" ) )
                {
                    rRaw.append( str ).append( "|" );
                }
                request = new Request( rRaw.toString(), remote );
                // Fetch the post request, if it exists
                if ( request.getQuery().getMethod() == Method.POST )
                {
                    final StringBuilder pR = new StringBuilder();
                    final int cl = Integer.parseInt( request.getHeader( "Content-Length" ).substring( 1 ) );
                    for ( int i = 0; i < cl; i++ )
                    {
                        pR.append( (char) input.read() );
                    }
                    request.setPostRequest( new PostRequest( pR.toString() ) );
                }
            } catch ( final Exception e )
            {
                e.printStackTrace();
                return;
            }
        }

        if ( !server.silent )
        {
            server.log( request.buildLog() );
        }

        handle( request, server, output );

        try
        {
            input.close();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    private static byte[] compress(final byte[] data) throws IOException {
        Assert.notNull( data );

        byte[] compressedData;
        try ( final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( data.length ) )
        {
            try ( final GZIPOutputStream gzipOutputStream = new GZIPOutputStream( byteArrayOutputStream ) )
            {
                gzipOutputStream.write( data );
            }
            compressedData = byteArrayOutputStream.toByteArray();
        }

        Assert.equals( compressedData != null && compressedData.length > 0, true );

        return compressedData;
    }

    private String md5Checksum(final byte[] input)
    {
        Assert.notNull( input );

        messageDigestMd5.reset();
        messageDigestMd5.update( input );
        return encoder.encode( messageDigestMd5.digest() );
    }
}