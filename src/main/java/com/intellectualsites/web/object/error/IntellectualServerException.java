package com.intellectualsites.web.object.error;

public class IntellectualServerException extends Exception {

    public IntellectualServerException(String s, Throwable cause) {
        super("IntellectualServer: " + s, cause);
    }

    public IntellectualServerException(String s) {
        super("IntellectualServer: " + s);
    }
}
