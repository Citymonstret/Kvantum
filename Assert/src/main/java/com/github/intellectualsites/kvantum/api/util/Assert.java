/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.util;

/**
 * Our custom assertion class. This
 * was solely made to give us more
 * control than we previously had.
 */
@SuppressWarnings("unused")
public class Assert
{

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

    public static void isFalse(final boolean a)
    {
        equals( a, false );
    }

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

    public static void notEmpty(final byte[] array)
    {
        equals( array != null && array.length > 0, true );
    }

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
