package com.intellectualsites.web.util;

import com.intellectualsites.web.object.error.AssertionError;

public class Assert {

    public static <T> void notEmpty(String s) {
        try {
            equals(s == null || s.isEmpty(), false);
        } catch (final AssertionError a) {
            throw new AssertionError(s, "was empty");
        }
    }

    public static void notNull(Object... in) {
        for (Object i : in) {
            notNull(i);
        }
    }

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
