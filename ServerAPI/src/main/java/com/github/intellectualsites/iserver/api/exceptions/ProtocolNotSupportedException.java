package com.github.intellectualsites.iserver.api.exceptions;

import com.github.intellectualsites.iserver.api.request.Request;

public class ProtocolNotSupportedException extends RequestException
{

    public ProtocolNotSupportedException(String message, Request request)
    {
        super( message, request );
    }
}
