package com.github.intellectualsites.kvantum.implementation;

import com.codahale.metrics.Timer;
import com.github.intellectualsites.kvantum.api.cache.CacheApplicable;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.core.WorkerProcedure;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.response.ResponseBody;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import com.github.intellectualsites.kvantum.api.views.errors.ViewException;
import org.apache.commons.lang3.ArrayUtils;
import xyz.kvantum.nanotube.Transformer;

import java.util.Map;
import java.util.Optional;

/**
 * Writes the response to the buffers in {@link WorkerContext}
 */
final class ResponseWriter extends Transformer<WorkerContext>
{

    private static final byte[] EMPTY = "NULL".getBytes();
    private static final String CONTENT_TYPE = "content_type";

    private final WorkerProcedure.WorkerProcedureInstance workerProcedureInstance = Server.getInstance()
            .getProcedure().getInstance();
    private final Kvantum server = Server.getInstance();

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        //
        // Scope variables
        //
        String textContent = "";
        byte[] bytes = EMPTY;
        boolean shouldCache = false;
        boolean cache = false;
        RequestHandler requestHandler = workerContext.getRequestHandler();
        AbstractRequest request = workerContext.getRequest();
        ResponseBody body;

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
                body = requestHandler.handle( request );
            } else
            { // Just read from memory
                body = server.getCacheManager().getCache( requestHandler );
            }

            //
            // If the body is null, it is either marked for an internal redirect
            // or something went wrong. In any case, abort.
            //
            if ( body == null )
            {
                final Object redirect = request.getMeta( "internalRedirect" );
                if ( redirect != null && redirect instanceof AbstractRequest )
                {
                    request = (AbstractRequest) redirect;
                    request.removeMeta( "internalRedirect" );
                    return this.handle( workerContext );
                }
                return null;
            }

            //
            // Store cache
            //
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

            //
            // Post-generation procedures
            //
            for ( final Map.Entry<String, String> postponedCookie : request.postponedCookies.entries() )
            {
                body.getHeader().setCookie( postponedCookie.getKey(), postponedCookie.getValue() );
            }

            //
            // Add the content type meta to the request, can then be used by worker processes
            //
            final Optional<String> contentType = body.getHeader().get( Header.HEADER_CONTENT_TYPE );
            if ( contentType.isPresent() )
            {
                request.addMeta( CONTENT_TYPE, contentType.get() );
            } else
            {
                request.addMeta( CONTENT_TYPE, null );
            }

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
                body = null;
            }
        }

        if ( body == null )
        {
            throw new ReturnStatus( Header.STATUS_INTERNAL_ERROR, workerContext );
        }

        workerContext.setBody( body );
        workerContext.setBytes( bytes );
        return workerContext;
    }
}
