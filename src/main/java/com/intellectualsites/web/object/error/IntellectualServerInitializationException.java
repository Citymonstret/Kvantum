package com.intellectualsites.web.object.error;

import com.intellectualsites.web.object.error.IntellectualServerException;

public class IntellectualServerInitializationException extends IntellectualServerException {

    public IntellectualServerInitializationException(String s, Throwable cause) {
        super("Couldn't load IntellectualServer: " + s, cause);
    }
}
