package com.intellectualsites.web.object;

public abstract class Filter {

    private final String key;

    public Filter(final String key) {
        this.key = key.toUpperCase();
    }

    public abstract Object handle(final Object in);

    @Override
    public final String toString() {
        return this.key;
    }
}
