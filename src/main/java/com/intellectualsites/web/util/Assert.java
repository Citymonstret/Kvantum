package com.intellectualsites.web.util;

import com.intellectualsites.web.object.error.AssertionError;

/**
 * Our custom assertion class. This
 * was solely made to give us more
 * control than we previously had.
 */
public class Assert {

    /**
     * Will only pass if the string isn't empty
     * @param s String to test
     * @return String, if passed
     * @throws AssertionError If not passing the test
     */
    public static String notEmpty(String s) {
        try {
            equals(s == null || s.isEmpty(), false);
        } catch (final AssertionError a) {
            throw new AssertionError(s, "was empty");
        }
        return s;
    }

    /**
     * Will only pass if the array contents aren't null
     * @param in Array to test
     * @return Array, if passed
     * @throws AssertionError If not passing the test
     */
    public static Object[] notNull(Object... in) {
        for (Object i : in) {
            notNull(i);
        }
        return in;
    }

    /**
     * Will only pass if the object isn't null
     * @param t Object to test
     * @param <T> Object type
     * @return T, if passed
     * @throws AssertionError If not passing the test
     */
    public static <T> T notNull(T t) {
        try {
            equals(t == null, false);
        } catch (final AssertionError a) {
            throw new AssertionError(t, "was null");
        }
        return t;
    }

    /**
     * Will only pass if a is equal to b
     * @param a Boolean A
     * @param b Boolean B
     * @param t Exception to cast
     * @param <Exception> Exception type
     * @throws Exception Exception to cast if a != b
     */
    public static <Exception extends Throwable> void equals(boolean a, boolean b, Exception t) throws Exception {
        if (a != b) {
            throw t;
        }
    }

    /**
     * Will only pass if a is equal to b
     * @param a Boolean A
     * @param b Boolean B
     * @throws AssertionError If not passing the test
     */
    public static void equals(boolean a, boolean b) {
        equals(a, b, new AssertionError(a, "a != b"));
    }
}
