package com.github.intellectualsites.iserver.api.exceptions;

public class RequestException extends IntellectualServerException
{

    public RequestException(String message)
    {
        super( "Failed to handle request: " + message );
    }
}
