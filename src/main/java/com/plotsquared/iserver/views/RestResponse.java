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
package com.plotsquared.iserver.views;

import com.plotsquared.iserver.matching.ViewPattern;
import com.plotsquared.iserver.object.HttpMethod;
import com.plotsquared.iserver.object.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public abstract class RestResponse
{

    private final HttpMethod httpMethod;
    private final ViewPattern viewPattern;

    public RestResponse(HttpMethod httpMethod, ViewPattern viewPattern)
    {
        this.httpMethod = httpMethod;
        this.viewPattern = viewPattern;
    }

    protected final boolean matches(Request request)
    {
        if ( !request.getQuery().getMethod().equals( httpMethod ) )
        {
            return false;
        }
        final Map<String, String> map = viewPattern.matches( request.getQuery().getFullRequest() );
        if ( map != null )
        {
            request.addMeta( "variables", map );
        }
        return map != null;
    }

    public abstract JSONObject generate(Request request) throws JSONException;

}
