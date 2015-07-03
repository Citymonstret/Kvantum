package com.intellectualsites.web.util;

import com.intellectualsites.web.object.AssertionError;

public class Assert {

    public static <T> void notNull(T t) {
        try {
            equals(t == null, false);
        } catch (final AssertionError a) {
            throw new AssertionError(t, "was null");
        }
    }

    public static <Exception extends Throwable> void equals(boolean a, boolean b, Exception t) throws Exception {
        if (a != b) {
            throw t;
        }
    }

    public static void equals(boolean a, boolean b) {
        equals(a, b, new AssertionError(a, "a != b"));
    }
}
