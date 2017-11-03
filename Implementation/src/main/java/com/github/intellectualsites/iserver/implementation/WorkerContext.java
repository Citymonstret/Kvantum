package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.cache.CacheApplicable;
import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.core.WorkerProcedure;
import com.github.intellectualsites.iserver.api.logging.LogModes;
import com.github.intellectualsites.iserver.api.logging.Logger;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Header;
import com.github.intellectualsites.iserver.api.response.ResponseBody;
import com.github.intellectualsites.iserver.api.session.ISession;
import com.github.intellectualsites.iserver.api.views.RequestHandler;
import com.github.intellectualsites.iserver.api.views.errors.ViewException;
import com.github.intellectualsites.iserver.implementation.error.IntellectualServerException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedOutputStream;
import java.util.Map;
import java.util.Optional;

@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@RequiredArgsConstructor
class WorkerContext
{

    private static final String CONTENT_TYPE = "content_type";
    private static byte[] EMPTY = "NULL".getBytes();

    private final IntellectualServer server;
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
        } catch ( final Exception e )
        {
            new IntellectualServerException( "Failed to flush to the client", e )
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
     * Load the request session
     */
    private void loadSession()
    {
        Optional<ISession> session = server.getSessionManager().getSession( request, output );
        if ( session.isPresent() )
        {
            this.request.setSession( session.get() );
            this.server.getSessionManager().setSessionLastActive( session.get().get( "id" ).toString() );
        } else
        {
            Logger.warn( "Could not initialize session!" );
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

    /**
     * Try to find a suitable handler for the request
     */
    private void matchRequestHandler()
    {
        this.requestHandler = server.getRouter().match( request );
    }

    void handle(final Worker worker)
    {
        //
        // Pre-handling
        //
        this.loadSession();
        this.matchRequestHandler();

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

        //
        // Send the response to the client
        //
        worker.sendToClient( body, bytes );

        if ( CoreConfig.debug )
        {
            server.log( "Request was served by '%s', with the type '%s'. The total length of the content was '%skB'",
                    requestHandler.getName(), body.isText() ? "text" : "bytes", ( bytes.length / 1000 )
            );
        }
    }

}
