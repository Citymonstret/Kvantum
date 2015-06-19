package com.intellectualsites.web.object;

public class IntellectualServerException extends Exception {

    public IntellectualServerException(String s, Throwable cause) {
        super("IntellectualServer: " + s, cause);
    }

    public IntellectualServerException(String s) {
        super("IntellectualServer: " + s);
    }
}
