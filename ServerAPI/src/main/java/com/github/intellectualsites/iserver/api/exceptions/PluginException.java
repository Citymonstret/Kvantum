package com.github.intellectualsites.iserver.api.exceptions;

public class PluginException extends IntellectualServerException
{

    public PluginException(final String message)
    {
        super( message );
    }

    public PluginException(final String message, final Throwable cause)
    {
        super( message, cause );
    }

}
