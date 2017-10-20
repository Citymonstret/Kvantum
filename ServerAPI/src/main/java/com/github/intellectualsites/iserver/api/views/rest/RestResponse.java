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
package com.github.intellectualsites.iserver.api.views.rest;

import com.github.intellectualsites.iserver.api.matching.ViewPattern;
import com.github.intellectualsites.iserver.api.request.HttpMethod;
import com.github.intellectualsites.iserver.api.request.Request;
import lombok.Getter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public abstract class RestResponse
{

    @Getter
    private final HttpMethod httpMethod;
    private final ViewPattern viewPattern;
    @Getter
    private final String contentType;
    private final ApiRequirements apiRequirements;

    public RestResponse(HttpMethod httpMethod, ViewPattern viewPattern)
    {
        this( httpMethod, viewPattern, "application/json" );
    }

    public RestResponse(HttpMethod httpMethod, ViewPattern viewPattern, String contentType)
    {
        this( httpMethod, viewPattern, contentType, new ApiRequirements() );
    }

    public RestResponse(HttpMethod httpMethod, ViewPattern viewPattern, String contentType, ApiRequirements
            apiRequirements)
    {
        this.httpMethod = httpMethod;
        this.viewPattern = viewPattern;
        this.contentType = contentType;
        this.apiRequirements = apiRequirements;
    }

    public boolean methodMatches(final Request request)
    {
        return request.getQuery().getMethod().equals( this.httpMethod );
    }

    public boolean contentTypeMatches(final Request request)
    {
        final String supplied = request.getHeader( "Accept" );
        if ( supplied.isEmpty() )
        {
            // Assume that they will accept everything
            return true;
        }
        final String[] parts = supplied.split( "\\s+" );
        for ( String part : parts )
        {
            if ( part.equals( this.contentType ) )
            {
                return true;
            }
        }
        return false;
    }

    protected final boolean matches(Request request)
    {
        final Map<String, String> map = viewPattern.matches( request.getQuery().getFullRequest() );
        if ( map != null )
        {
            request.addMeta( "variables", map );
        }
        return map != null;
    }

    public abstract JSONObject generate(Request request) throws JSONException;

}
