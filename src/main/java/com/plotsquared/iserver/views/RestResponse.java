package com.plotsquared.iserver.views;

import com.plotsquared.iserver.http.HttpMethod;
import com.plotsquared.iserver.matching.ViewPattern;
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
