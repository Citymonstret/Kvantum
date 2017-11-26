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

import xyz.kvantum.nanotube.ConditionalTransformer;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.request.post.DummyPostRequest;
import xyz.kvantum.server.api.request.post.EntityType;
import xyz.kvantum.server.api.request.post.JsonPostRequest;
import xyz.kvantum.server.api.request.post.MultipartPostRequest;
import xyz.kvantum.server.api.request.post.PostRequest;
import xyz.kvantum.server.api.request.post.UrlEncodedPostRequest;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.util.Assert;

/**
 * Makes sure that any provided {@link PostRequest}
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

        if ( ( isFormURLEncoded = contentType.startsWith( "application/x-www-form-urlencoded" ) ) ||
                ( isJSON = EntityType.JSON.getContentType().startsWith( contentType ) ) )
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
