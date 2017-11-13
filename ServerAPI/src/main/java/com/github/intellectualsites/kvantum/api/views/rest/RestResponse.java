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

import com.github.intellectualsites.kvantum.api.matching.ViewPattern;
import com.github.intellectualsites.kvantum.api.request.HttpMethod;
import com.github.intellectualsites.kvantum.api.request.Request;
import lombok.AccessLevel;
import lombok.Getter;
import org.json.simple.JSONObject;

import java.util.Map;

public abstract class RestResponse
{

    @Getter
    private final HttpMethod httpMethod;
    private final ViewPattern viewPattern;
    @Getter
    private final String contentType;
    @Getter(AccessLevel.PROTECTED)
    private final RequestRequirements requestRequirements;

    public RestResponse(HttpMethod httpMethod, ViewPattern viewPattern)
    {
        this( httpMethod, viewPattern, "application/json" );
    }

    public RestResponse(HttpMethod httpMethod, ViewPattern viewPattern, String contentType)
    {
        this( httpMethod, viewPattern, contentType, new RequestRequirements() );
    }

    public RestResponse(HttpMethod httpMethod, ViewPattern viewPattern, String contentType, RequestRequirements
            requestRequirements)
    {
        this.httpMethod = httpMethod;
        this.viewPattern = viewPattern;
        this.contentType = contentType;
        this.requestRequirements = requestRequirements;
    }

    public boolean methodMatches(final Request request)
    {
        return request.getQuery().getMethod().equals( this.httpMethod );
    }

    public boolean contentTypeMatches(final Request request)
    {
        if ( this.contentType.isEmpty() )
        {
            // We simply don't care.
            return true;
        }
        final String supplied = request.getHeader( "Accept" );
        if ( supplied.isEmpty() )
        {
            // Assume that they will accept everything
            return true;
        }
        final String[] parts = supplied.split( "\\s+" );
        for ( String part : parts )
        {
            if ( part.equals( "*/*" ) || part.equals( this.contentType ) )
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

    public abstract JSONObject generate(Request request);

}
