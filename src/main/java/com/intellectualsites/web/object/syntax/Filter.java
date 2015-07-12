package com.intellectualsites.web.object.syntax;

public abstract class Filter {

    private final String key;

    public Filter(final String key) {
        this.key = key.toUpperCase();
    }

    public abstract Object handle(final String objectName, final Object in);

    @Override
    public final String toString() {
        return this.key;
    }
}
