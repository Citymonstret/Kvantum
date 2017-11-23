package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.request.HttpMethod;
import com.github.intellectualsites.kvantum.api.request.post.DummyPostRequest;
import com.github.intellectualsites.kvantum.api.request.post.EntityType;
import com.github.intellectualsites.kvantum.api.request.post.JsonPostRequest;
import com.github.intellectualsites.kvantum.api.request.post.MultipartPostRequest;
import com.github.intellectualsites.kvantum.api.request.post.UrlEncodedPostRequest;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.util.Assert;
import xyz.kvantum.nanotube.ConditionalTransformer;

/**
 * Makes sure that any provided {@link com.github.intellectualsites.kvantum.api.request.post.PostRequest}
 * is read into the {@link AbstractRequest}
 */
final class PostRequestGenerator extends ConditionalTransformer<WorkerContext>
{

    PostRequestGenerator()
    {
        super( workerContext -> workerContext.getRequest().getQuery().getMethod() == HttpMethod.POST );
    }

    @Override
    protected WorkerContext handle(WorkerContext workerContext) throws Throwable
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
                throw new ReturnStatus( Header.STATUS_BAD_REQUEST, workerContext );
            }
            if ( contentLength >= CoreConfig.Limits.limitPostBasicSize )
            {
                if ( CoreConfig.debug )
                {
                    Logger.debug( "Supplied post body size too large (%s > %s)", contentLength,
                            CoreConfig.Limits.limitPostBasicSize );
                }
                throw new ReturnStatus( Header.STATUS_ENTITY_TOO_LARGE, workerContext );
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
        return workerContext;
    }
}
