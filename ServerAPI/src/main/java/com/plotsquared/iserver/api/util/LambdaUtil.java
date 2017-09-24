/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class LambdaUtil
{

    public static <T> Optional<T> getFirst(final Collection<T> collection, final Predicate<T> predicate)
    {
        Assert.notNull( collection, predicate );

        return collection.stream().filter( predicate ).findFirst();
    }

    public static <T> Optional<T> getFirst(final T[] collection, final Predicate<T> predicate)
    {
        Assert.notNull( collection, predicate );

        return Arrays.stream( collection ).filter( predicate ).findFirst();
    }

    public static <T> Collection<T> collectionAssign(final Provider<Collection<T>> listProvider, final Provider<T>
            valueProvider, final int number)
    {
        Assert.notNull( listProvider, valueProvider );
        Assert.isPositive( number );

        final Collection<T> list = listProvider.provide();
        for ( int i = 0; i < number; i++ )
        {
            list.add( valueProvider.provide() );
        }
        return list;
    }

    public static <T> T[] arrayAssign(final T[] array, final Provider<T> provider)
    {
        Assert.notNull( array, provider );

        for ( int i = 0; i < array.length; i++ )
        {
            array[ i ] = provider.provide();
        }
        return array;
    }

    public static <T> void arrayForeach(final T[] array, final Consumer<T> consumer)
    {
        Assert.notNull( array, consumer );

        Arrays.stream( array ).forEach( consumer );

    }

    @SafeVarargs
    public static <T> void arrayForeach(final Consumer<T> consumer, final T... array)
    {
        Assert.notNull( array, consumer );

        Arrays.stream( array ).forEach( consumer );
    }


    public static <T> void arrayForeach(final T[] array, final Predicate<T> filter, final Consumer<T> consumer)
    {
        Assert.notNull( array, filter, consumer );

        Arrays.stream( array ).filter( filter ).forEach( consumer );
    }

}
