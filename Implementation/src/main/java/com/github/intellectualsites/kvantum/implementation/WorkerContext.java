/*
 * Kvantum is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.kvantum.implementation;

import com.codahale.metrics.Timer;
import com.github.intellectualsites.kvantum.api.cache.CacheApplicable;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.core.WorkerProcedure;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.response.ResponseBody;
import com.github.intellectualsites.kvantum.api.util.ProtocolType;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import com.github.intellectualsites.kvantum.api.views.errors.ViewException;
import com.github.intellectualsites.kvantum.api.views.requesthandler.HTTPSRedirectHandler;
import com.github.intellectualsites.kvantum.implementation.error.KvantumException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedOutputStream;
import java.net.SocketException;
import java.util.Map;
import java.util.Optional;

@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@RequiredArgsConstructor
class WorkerContext
{

    private static final String CONTENT_TYPE = "content_type";
    private static byte[] EMPTY = "NULL".getBytes();

    private final Kvantum server;
    private final WorkerProcedure.WorkerProcedureInstance workerProcedureInstance;

    private RequestHandler requestHandler;
    private Request request;
    private BufferedOutputStream output;
    private ResponseBody body;
    private boolean gzip;

    /**
     * Flush the output stream (I.e. send the stored bytes to the client)
     */
    void flushOutput()
    {
        try
        {
            output.flush();
        } catch ( final SocketException e )
        {
            if ( e.getMessage().equalsIgnoreCase( "Connection closed by remote host" ) && CoreConfig.debug )
            {
                Logger.warn( "Failed to serve request [%s]: Remote connection closed.", request );
            }
        } catch ( final Exception e )
        {
            new KvantumException( "Failed to flush to the client", e )
                    .printStackTrace();
        }
    }

    /**
     * Make sure that all postponed cookies are applied to the body
     */
    private void handlePostponedCookies()
    {
        for ( final Map.Entry<String, String> postponedCookie : request.postponedCookies.entries() )
        {
            body.getHeader().setCookie( postponedCookie.getKey(), postponedCookie.getValue() );
        }
    }

    /**
     * Add the content type meta to the request, can then be used by worker processes
     */
    private void fixContentTypeHeader()
    {
        final Optional<String> contentType = body.getHeader().get( Header.HEADER_CONTENT_TYPE );
        if ( contentType.isPresent() )
        {
            request.addMeta( CONTENT_TYPE, contentType.get() );
        } else
        {
            request.addMeta( CONTENT_TYPE, null );
        }
    }

    /**
     * <p>
     * Determine whether or not GZIP compression should be used.
     * This depends on two things:
     * <ol>
     * <li>If GZIP compression is enabled in {@link CoreConfig}</li>
     * <li>If the client has sent a "Accept-Encoding" header</li>
     * </ol>
     * </p>
     * <p>
     * The value can be fetched using {@link #isGzip()}
     * </p>
     */
    void determineGzipStatus()
    {
        if ( CoreConfig.gzip )
        {
            if ( request.getHeader( "Accept-Encoding" ).contains( "gzip" ) )
            {
                this.gzip = true;
                body.getHeader().set( Header.HEADER_CONTENT_ENCODING, "gzip" );
            } else if ( CoreConfig.debug )
            {
                Message.CLIENT_NOT_ACCEPTING_GZIP.log( request.getHeaders() );
            }
        } else
        {
            this.gzip = false;
        }
    }

    void handle(final Worker worker)
    {
        //
        // Pre-handling
        //
        this.request.setOutputStream( this.output );
        if ( CoreConfig.Sessions.autoLoad )
        {
            final Timer.Context context = ServerImplementation.getImplementation().getMetrics()
                    .registerSessionPreparation();
            this.request.requestSession();
            context.stop();
        }
        this.requestHandler = server.getRouter().match( request );

        if ( this.requestHandler == null )
        {
            worker.handleSendStatusOnly( Header.STATUS_NOT_FOUND );
            return;
        }

        if ( request.getProtocolType() != ProtocolType.HTTPS && this.requestHandler.forceHTTPS() )
        {
            if ( CoreConfig.debug )
            {
                Logger.debug( "Redirecting request [%s] to HTTPS version of [%s]", request, requestHandler );
            }
            if ( !CoreConfig.SSL.enable )
            {
                Logger.error( "RequestHandler (%s) forces HTTPS but SSL runner not enabled!" );
                worker.handleSendStatusOnly( Header.STATUS_INTERNAL_ERROR );
                return;
            }
            //
            // TODO: Consider moving this to its own class...
            //
            this.requestHandler = HTTPSRedirectHandler.getInstance();
        }

        //
        // Scope variables
        //
        String textContent = "";
        byte[] bytes = EMPTY;
        boolean shouldCache = false;
        boolean cache = false;

        try
        {
            if ( !requestHandler.getValidationManager().isEmpty() )
            {
                requestHandler.getValidationManager().validate( request );
            }

            //
            // Try to find cached response
            //
            if ( CoreConfig.Cache.enabled && requestHandler instanceof CacheApplicable
                    && ( (CacheApplicable) requestHandler ).isApplicable( request ) )
            {
                cache = true;
                if ( !server.getCacheManager().hasCache( requestHandler ) )
                {
                    shouldCache = true;
                }
            }

            //
            // Make sure that cache is handled as it should
            //
            if ( !cache || shouldCache )
            { // Either it's a non-cached view, or there is no cache stored
                this.body = requestHandler.handle( request );
            } else
            { // Just read from memory
                this.body = server.getCacheManager().getCache( requestHandler );
            }

            //
            // If the body is null, it is either marked for an internal redirect
            // or something went wrong. In any case, abort.
            //
            if ( this.body == null )
            {
                final Object redirect = request.getMeta( "internalRedirect" );
                if ( redirect != null && redirect instanceof Request )
                {
                    this.request = (Request) redirect;
                    this.request.removeMeta( "internalRedirect" );
                    handle( worker );
                }
                return;
            }

            //
            // Store cache
            //
            if ( shouldCache )
            {
                server.getCacheManager().setCache( requestHandler, this.body );
            }

            if ( this.body.isText() )
            {
                textContent = this.body.getContent();
            } else
            {
                bytes = this.body.getBytes();
            }

            //
            // Post-generation procedures
            //
            handlePostponedCookies();
            fixContentTypeHeader();

            //
            // Turn response into a byte array
            //
            final Timer.Context metricContext = ServerImplementation.getImplementation().getMetrics()
                    .registerContentHandling();
            if ( request.getQuery().getMethod().hasBody() )
            {
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
            }
            metricContext.stop();
        } catch ( final Exception e )
        {
            Message.WORKER_FAILED_HANDLING.log( e.getMessage() );

            if ( CoreConfig.verbose )
            {
                e.printStackTrace();
            }

            if ( CoreConfig.debug )
            {
                body = new ViewException( e ).generate( request );
                bytes = body.getContent().getBytes();
            } else
            {
                worker.handleSendStatusOnly( Header.STATUS_INTERNAL_ERROR );
                return;
            }
        }

        //
        // Send the response to the client
        //
        worker.sendToClient( body, bytes );

        if ( CoreConfig.debug )
        {
            Message.REQUEST_SERVED.log( requestHandler.getName(),
                    body.isText() ? "text" : "bytes", ( bytes.length / 1000 ) );
        }
    }

}
