/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.server.api.views.rest;

import lombok.AccessLevel;
import lombok.Getter;
import org.json.simple.JSONObject;
import xyz.kvantum.server.api.matching.ViewPattern;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;

import java.util.Map;

@SuppressWarnings({ "unused", "WeakerAccess " })
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

    public boolean methodMatches(final AbstractRequest request)
    {
        return request.getQuery().getMethod().equals( this.httpMethod );
    }

    public boolean contentTypeMatches(final AbstractRequest request)
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

    protected final boolean matches(AbstractRequest request)
    {
        final Map<String, String> map = viewPattern.matches( request.getQuery().getFullRequest() );
        if ( map != null )
        {
            request.addMeta( "variables", map );
        }
        return map != null;
    }

    public abstract JSONObject generate(AbstractRequest request);

}
