package com.intellectualsites.web.object;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class HeaderOption {

    @NonNull
    @Getter
    private final String text;

    @Getter
    private boolean cacheApplicable = true;

    public static HeaderOption create(final String text) {
        return new HeaderOption(text);
    }

    public static HeaderOption create(final String text, boolean cacheApplicable) {
        HeaderOption option = new HeaderOption(text);
        option.cacheApplicable = cacheApplicable;
        return option;
    }

    @Override
    public final String toString() {
        return this.text;
    }
}
