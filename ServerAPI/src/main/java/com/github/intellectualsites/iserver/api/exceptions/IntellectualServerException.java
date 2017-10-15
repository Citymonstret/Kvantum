package com.github.intellectualsites.iserver.api.exceptions;

public class IntellectualServerException extends RuntimeException
{

    public IntellectualServerException(final String message)
    {
        super( "IntellectualServer threw an exception: " + message );
    }

    public IntellectualServerException(final String message, final Throwable cause)
    {
        super( "IntellectualServer threw an exception: " + message, cause );
    }

    public IntellectualServerException(final Throwable cause)
    {
        super( cause );
    }

}
