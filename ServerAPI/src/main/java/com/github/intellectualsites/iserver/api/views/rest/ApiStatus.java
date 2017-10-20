package com.github.intellectualsites.iserver.api.views.rest;

import org.json.JSONException;
import org.json.JSONObject;

public enum ApiStatus
{
    SUCCESS,
    FAILURE,
    ERROR, PENDING;

    public JSONObject getJSONObject() throws JSONException
    {
        final JSONObject object = new JSONObject();
        object.put( "statusCode", this.ordinal() );
        object.put( "status", this.toString().toLowerCase() );
        return object;
    }

}
