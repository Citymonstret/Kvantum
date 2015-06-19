package com.intellectualsites.web.object;

public class IntellectualServerInitializationException extends IntellectualServerException {

    public IntellectualServerInitializationException(String s, Throwable cause) {
        super("Couldn't load IntellectualServer: " + s, cause);
    }
}
