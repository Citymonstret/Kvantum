package com.github.intellectualsites.iserver.api.util;

/**
 * Our custom assertion class. This
 * was solely made to give us more
 * control than we previously had.
 */
public class Assert
{

    /**
     * Will only pass if the string isn't empty
     *
     * @param s String to test
     * @return String, if passed
     * @throws com.github.intellectualsites.iserver.api.util.AssertionError If not passing the test
     */
    public static String notEmpty(final String s)
    {
        try
        {
            equals( s == null || s.isEmpty(), false );
        } catch ( final com.github.intellectualsites.iserver.api.util.AssertionError a )
        {
            throw new com.github.intellectualsites.iserver.api.util.AssertionError( s, "was empty" );
        }
        return s;
    }

    /**
     * Will only pass if the array contents aren't null
     *
     * @param in Array to test
     * @return Array, if passed
     * @throws com.github.intellectualsites.iserver.api.util.AssertionError If not passing the test
     */
    public static Object[] notNull(final Object... in)
    {
        for ( final Object i : in )
        {
            notNull( i );
        }
        return in;
    }

    public static void notEmpty(final Object[] array)
    {
        equals( array != null && array.length > 0, true );
    }

    public static Number isPositive(final Number number)
    {
        equals( number.intValue() >= 0, true );
        return number;
    }

    /**
     * Will only pass if the object isn't null
     *
     * @param t   Object to test
     * @param <T> Object type
     * @return T, if passed
     * @throws com.github.intellectualsites.iserver.api.util.AssertionError If not passing the test
     */
    public static <T> T notNull(final T t)
    {
        try
        {
            equals( t == null, false );
        } catch ( final com.github.intellectualsites.iserver.api.util.AssertionError a )
        {
            throw new com.github.intellectualsites.iserver.api.util.AssertionError( t, "was null" );
        }
        return t;
    }

    public static <T extends Validatable> T isValid(final T t)
    {
        notNull( t );
        try
        {
            equals( t.isValid(), true );
        } catch ( final com.github.intellectualsites.iserver.api.util.AssertionError a )
        {
            throw new com.github.intellectualsites.iserver.api.util.AssertionError( t, "was invalid" );
        }
        return t;
    }

    /**
     * Will only pass if a is equal to b
     *
     * @param a           Boolean A
     * @param b           Boolean B
     * @param t           Exception to cast
     * @param <Exception> Exception type
     * @throws Exception Exception to cast if a != b
     */
    public static <Exception extends Throwable> void equals(final boolean a, final boolean b,
                                                            final Exception t) throws Exception
    {
        if ( a != b )
        {
            throw t;
        }
    }

    /**
     * Will only pass if a is equal to b
     *
     * @param a Boolean A
     * @param b Boolean B
     * @throws com.github.intellectualsites.iserver.api.util.AssertionError If not passing the test
     */
    public static void equals(final boolean a, final boolean b)
    {
        equals( a, b, new com.github.intellectualsites.iserver.api.util.AssertionError( a, "a != b" ) );
    }

    public static void equals(final int a, final int b)
    {
        equals( a == b, true, new com.github.intellectualsites.iserver.api.util.AssertionError( a, a + " != " + b ) );
    }

    /**
     * Will only pass if a is equal to b
     *
     * @param a       Boolean A
     * @param b       Boolean B
     * @param message Error message
     * @throws com.github.intellectualsites.iserver.api.util.AssertionError If not passing the test
     */
    public static void equals(final boolean a, final boolean b, String message)
    {
        equals( a, b, new com.github.intellectualsites.iserver.api.util.AssertionError( a, message ) );
    }

    public static void notEmpty(final byte[] array)
    {
        equals( array != null && array.length > 0, true );
    }
}
