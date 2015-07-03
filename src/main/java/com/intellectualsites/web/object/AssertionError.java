package com.intellectualsites.web.object;

public class AssertionError extends RuntimeException {

    public AssertionError(Object o, String s) {
        super("'" + o + "' didn't pass the assertion check; " + s);
    }

}
