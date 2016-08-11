package com.plotsquared.iserver.object;

import com.plotsquared.iserver.util.Assert;

final class HeaderOption {

    private final String text;
    private boolean cacheApplicable = true;

    private HeaderOption(final String text) {
        Assert.notNull(text);

        this.text = text;
    }

    public static HeaderOption create(final String text) {
        return new HeaderOption(text);
    }

    public static HeaderOption create(final String text, boolean cacheApplicable) {
        return new HeaderOption(text).cacheApplicable(cacheApplicable);
    }

    public String getText() {
        return text;
    }

    public boolean isCacheApplicable() {
        return cacheApplicable;
    }

    private HeaderOption cacheApplicable(final boolean b) {
        this.cacheApplicable = b;
        return this;
    }

    @Override
    public final String toString() {
        return this.text;
    }

}
