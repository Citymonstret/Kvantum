package com.intellectualsites.web.object;

public class IntellectualServerStartException extends IntellectualServerException {

    public IntellectualServerStartException(String s, Throwable cause) {
        super("Couldn't start IntellectualServer: " + s, cause);
    }
}
