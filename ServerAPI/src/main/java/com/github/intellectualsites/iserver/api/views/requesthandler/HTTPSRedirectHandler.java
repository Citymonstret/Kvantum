package com.github.intellectualsites.iserver.api.views.requesthandler;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.logging.Logger;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Response;
import com.github.intellectualsites.iserver.api.util.StringUtil;
import lombok.Getter;

import java.util.function.BiConsumer;

/**
 * {@link com.github.intellectualsites.iserver.api.views.RequestHandler RequestHandler}
 * responsible for {@link com.github.intellectualsites.iserver.api.util.ProtocolType#HTTP HTTP} ->
 * {@link com.github.intellectualsites.iserver.api.util.ProtocolType#HTTP HTTPS } rerouting
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
