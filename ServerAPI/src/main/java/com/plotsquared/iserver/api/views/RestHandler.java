/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.views;

import com.plotsquared.iserver.api.request.Request;
import com.plotsquared.iserver.api.response.Header;
import com.plotsquared.iserver.api.response.Response;
import com.plotsquared.iserver.api.util.Assert;
import com.plotsquared.iserver.api.util.IgnoreSyntax;
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
        final RestResponse restResponse = (RestResponse) request.getMeta( "restResponse" );
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
