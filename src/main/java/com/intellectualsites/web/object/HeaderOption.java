package com.intellectualsites.web.object;

import lombok.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class HeaderOption {

    @NonNull
    @Getter
    private final String text;

    @Getter
    private boolean cacheApplicable = true;

    public static HeaderOption create(@NonNull final String text) {
        return new HeaderOption(text);
    }

    private HeaderOption cacheApplicable(final boolean b) {
        this.cacheApplicable = b;
        return this;
    }

    public static HeaderOption create(@NonNull final String text, boolean cacheApplicable) {
        return new HeaderOption(text).cacheApplicable(cacheApplicable);
    }

    @Override
    public final String toString() {
        return this.text;
    }

}
