/*
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.util;

/**
 * Our custom assertion class. This
 * was solely made to give us more
 * control than we previously had.
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public final class Assert
{

    /**
     * Private no-args constructor to prevent initialization
     */
    private Assert()
    {
    }

    /**
     * Will only pass if the string isn't empty
     *
     * @param s String to test
     * @return String, if passed
     * @throws AssertionError If not passing the test
     */
    public static String notEmpty(final String s)
    {
        try
        {
            equals( s == null || s.isEmpty(), false );
        } catch ( final AssertionError a )
        {
            throw new AssertionError( s, "was empty" );
        }
        return s;
    }

    /**
     * Will only pass if the array contents aren't null
     *
     * @param in Array to test
     * @return Array, if passed
     * @throws AssertionError If not passing the test
     */
    public static Object[] notNull(final Object... in)
    {
        for ( final Object i : in )
        {
            notNull( i );
        }
        return in;
    }

    /**
     * Assertion that will pass if, and only if, the given array
     * is neither null nor empty
     *
     * @param array Array to be tested
     * @return The given array
     */
    public static <T> T[] notEmpty(final T[] array)
    {
        equals( array != null && array.length > 0, true );
        return array;
    }

    /**
     * Assertion that will pass, and only if, the given number
     * is neither null nor negative (can be 0)
     *
     * @param number Number to be tested
     * @return the number
     */
    public static Number isPositive(final Number number)
    {
        equals( notNull( number ).intValue() >= 0, true );
        return number;
    }

    /**
     * Will only pass if the object isn't null
     *
     * @param t   Object to test
     * @param <T> Object type
     * @return T, if passed
     * @throws AssertionError If not passing the test
     */
    public static <T> T notNull(final T t)
    {
        try
        {
            equals( t == null, false );
        } catch ( final AssertionError a )
        {
            throw new AssertionError( t, "was null" );
        }
        return t;
    }

    /**
     * Assertion that will pass if a given {@link Validatable} is
     * valid, as stated by: {@link Validatable#isValid()}
     *
     * @param t   {@link Validatable} object
     * @param <T> Type
     * @return The obhect
     */
    public static <T extends Validatable> T isValid(final T t)
    {
        notNull( t );
        try
        {
            equals( t.isValid(), true );
        } catch ( final AssertionError a )
        {
            throw new AssertionError( t, "was invalid" );
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
     * Assertion that will pass if, and only if, the
     * given boolean is false
     *
     * @param a Boolean to be tested
     */
    public static void isFalse(final boolean a)
    {
        equals( a, false );
    }

    /**
     * Assertion that will pass if, and only if, the given
     * boolean is true
     *
     * @param a Boolean to be tested
     */
    public static void isTrue(final boolean a)
    {
        equals( a, true );
    }

    /**
     * Will only pass if a is equal to b
     *
     * @param a Boolean A
     * @param b Boolean B
     * @throws AssertionError If not passing the test
     */
    public static void equals(final boolean a, final boolean b)
    {
        equals( a, b, new AssertionError( a, "a != b" ) );
    }

    /**
     * Assertion that will pass if, and only if, the given
     * integers A and B are equal
     *
     * @param a Integer A
     * @param b Integer B
     */
    public static void equals(final int a, final int b)
    {
        equals( a == b, true, new AssertionError( a, a + " != " + b ) );
    }

    /**
     * Will only pass if a is equal to b
     *
     * @param a       Boolean A
     * @param b       Boolean B
     * @param message Error message
     * @throws AssertionError If not passing the test
     */
    public static void equals(final boolean a, final boolean b, String message)
    {
        equals( a, b, new AssertionError( a, message ) );
    }

    /**
     * Assertion that will pass if, and only if, the
     * given object is not null
     *
     * @param o   Object to be tested
     * @param <T> Object type
     * @return Object
     */
    public static <T> T isNull(final T o)
    {
        try
        {
            equals( o == null, true );
        } catch ( final AssertionError a )
        {
            throw new AssertionError( o, "was not null" );
        }
        return o;
    }
}
