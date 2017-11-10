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
package com.github.intellectualsites.kvantum.api.views.requesthandler;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.util.StringUtil;
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

    private static final BiConsumer<Request, Response> responseGenerator = (request, response) ->
    {
        final Request.Query query = request.getQuery();
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
                    .append( StringUtil.join( query.getParameters(), "=", "&" ) );
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
