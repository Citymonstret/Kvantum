package com.github.intellectualsites.iserver.api.exceptions;

import com.github.intellectualsites.iserver.api.request.Request;

final public class QueryException extends RequestException
{

    public QueryException(String message, Request request)
    {
        super( "Failed to interpret query: " + message, request );
    }
}
