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
package com.github.intellectualsites.kvantum.api.views.rest;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.IgnoreSyntax;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RestHandler extends RequestHandler implements IgnoreSyntax
{

    private static final Response RESPONSE_METHOD_NOT_ALLOWED = new Response()
            .setHeader( new Header( Header.STATUS_NOT_ALLOWED ) );
    private static final Response RESPONSE_NOT_ACCEPTABLE = new Response()
            .setHeader( new Header( Header.STATUS_NOT_ACCEPTABLE ) );

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
    public boolean matches(final AbstractRequest request)
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
    public Response generate(AbstractRequest request)
    {
        final RestResponse restResponse = (RestResponse) request.getMeta( "restResponse" );
        if ( restResponse == null )
        {
            return null;
        }

        if ( !restResponse.methodMatches( request ) )
        {
            return RESPONSE_METHOD_NOT_ALLOWED;
        }

        if ( !restResponse.contentTypeMatches( request ) )
        {
            return RESPONSE_NOT_ACCEPTABLE;
        }

        JSONObject jsonObject = null;
        try
        {
            jsonObject = restResponse.generate( request );
        } catch ( final Exception e )
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
        response.getHeader().set( Header.X_CONTENT_TYPE_OPTIONS, "nosniff" );
        response.getHeader().set( Header.X_FRAME_OPTIONS, "deny" );
        response.getHeader().set( Header.CONTENT_SECURITY_POLICY, "default-src 'none'" );
        response.setContent( jsonObject.toString() );
        return response;
    }

    @Override
    public String getName()
    {
        return "RESTHandler";
    }

    /**
     * OVERRIDE ME
     *
     * @return false
     */
    @Override
    public boolean forceHTTPS()
    {
        return false;
    }

}
