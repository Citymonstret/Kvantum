package com.plotsquared.iserver.views;

import com.plotsquared.iserver.crush.syntax.IgnoreSyntax;
import com.plotsquared.iserver.object.Header;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.Assert;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RestHandler extends RequestHandler implements IgnoreSyntax
{

    private final List<RestResponse> responseHandlers;

    public RestHandler()
    {
        this.responseHandlers = new ArrayList<>();
    }

    public void registerHandler(final RestResponse restResponse)
    {
        this.responseHandlers.add( restResponse );
    }

    @Override
    public boolean matches(final Request request)
    {
        Assert.isValid( request );

        for ( final RestResponse restResponse : responseHandlers )
        {
            if ( restResponse.matches( request ) )
            {
                request.addMeta( "restResponse", restResponse );
                return true;
            }
        }
        return false;
    }

    @Override
    public Response generate(Request request)
    {
        final RestResponse restResponse = ( RestResponse ) request.getMeta( "restResponse" );
        if ( restResponse == null )
        {
            return null;
        }
        JSONObject jsonObject = null;
        try
        {
            jsonObject = restResponse.generate( request );
        } catch ( JSONException e )
        {
            e.printStackTrace();
            try
            {
                jsonObject = new JSONObject();
                jsonObject.put( "status", "error" );
                jsonObject.put( "error", e );
            } catch ( final Exception ignored )
            {
            }
        }

        assert jsonObject != null; // Bypass compilation warnings

        final Response response = new Response( this );
        response.getHeader().set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_JSON );
        response.setContent( jsonObject.toString() );
        return response;
    }

    @Override
    public String getName()
    {
        return "RESTHandler";
    }

}
