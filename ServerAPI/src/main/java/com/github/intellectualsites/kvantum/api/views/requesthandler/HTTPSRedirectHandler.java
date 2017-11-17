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
package com.github.intellectualsites.kvantum.api.views.requesthandler;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.util.MapUtil;
import com.github.intellectualsites.kvantum.api.util.ProtocolType;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import lombok.Getter;

import java.util.function.BiConsumer;

/**
 * {@link RequestHandler RequestHandler}
 * responsible for {@link ProtocolType#HTTP HTTP} ->
 * {@link ProtocolType#HTTP HTTPS } rerouting
 */
final public class HTTPSRedirectHandler extends SimpleRequestHandler
{

    private static final BiConsumer<AbstractRequest, Response> responseGenerator = (request, response) ->
    {
        final AbstractRequest.Query query = request.getQuery();
        final StringBuilder urlBuilder = new StringBuilder( "https://" )
                .append( CoreConfig.webAddress );
        if ( CoreConfig.SSL.port != 443 )
        {
            urlBuilder.append( ":" ).append( CoreConfig.SSL.port );
        }
        urlBuilder.append( query.getResource() );
        if ( !query.getResource().endsWith( "/" ) )
        {
            urlBuilder.append( "/" );
        }
        if ( !query.getParameters().isEmpty() )
        {
            urlBuilder.append( "?" )
                    .append( MapUtil.join( query.getParameters(), "=", "&" ) );
        }
        response.getHeader().redirect( urlBuilder.toString() );
        if ( CoreConfig.debug )
        {
            Logger.debug( "Generated HTTPS url: " + urlBuilder.toString() );
        }
        final String responseBodyBuilder = "<h1>Redirecting...</h1>\n<p>If the request isn't redirecting," +
                " click: " +
                "<a href=\"" +
                urlBuilder.toString() +
                "\" title=\"HTTPS Redirect\">" +
                urlBuilder.toString() +
                "</a>";
        response.setContent( responseBodyBuilder );
    };

    @Getter
    private static final HTTPSRedirectHandler instance = new HTTPSRedirectHandler();

    private HTTPSRedirectHandler()
    {
        super( "", responseGenerator );
    }

}
