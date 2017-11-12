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

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility class for lambda based operations
 */
@SuppressWarnings("ALL")
@UtilityClass
public final class LambdaUtil
{

    /**
     * Attempt to get the first object in a collection that matches the given predicate
     *
     * @param collection Collection
     * @param predicate  Predicate
     * @param <T>        Object type
     * @return Either the found object, or null
     */
    public static <T> Optional<T> getFirst(final Collection<T> collection, final Predicate<T> predicate)
    {
        Assert.notNull( collection, predicate );

        return collection.stream().filter( predicate ).findFirst();
    }

    /**
     * Attempt to get the first object in an array that matches the given predicate
     *
     * @param array     Array
     * @param predicate Predicate
     * @param <T>       Object type
     * @return Either the found object, or null
     */
    public static <T> Optional<T> getFirst(final T[] array, final Predicate<T> predicate)
    {
        Assert.notNull( array, predicate );

        return Arrays.stream( array ).filter( predicate ).findFirst();
    }

    /**
     * Create a new collection and assign a set number of objects to it
     *
     * @param collectionProvider Provider for the collection
     * @param valueProvider      Provider for the values
     * @param number             Number of items that should be assigned
     * @param <T>                Type
     * @return assigned collection
     */
    public static <T> Collection<T> collectionAssign(final Provider<Collection<T>> collectionProvider,
                                                     final Provider<T>
                                                             valueProvider, final int number)
    {
        Assert.notNull( collectionProvider, valueProvider );
        Assert.isPositive( number );

        final Collection<T> list = collectionProvider.provide();
        for ( int i = 0; i < number; i++ )
        {
            list.add( valueProvider.provide() );
        }
        return list;
    }

    /**
     * Assign items to an array
     *
     * @param array    Array
     * @param provider Provider for the values
     * @param <T>      Type
     * @return assigned array
     */
    public static <T> T[] arrayAssign(final T[] array, final Provider<T> provider)
    {
        Assert.notNull( array, provider );

        for ( int i = 0; i < array.length; i++ )
        {
            array[ i ] = provider.provide();
        }
        return array;
    }

    /**
     * Perform an action for each item in an array
     *
     * @param array    Array
     * @param consumer Action
     * @param <T>      Type
     */
    public static <T> void arrayForeach(final T[] array, final Consumer<T> consumer)
    {
        Assert.notNull( array, consumer );

        Arrays.stream( array ).forEach( consumer );

    }

    /**
     * Perform an action for each item in an array (vararg...)
     *
     * @param consumer Action
     * @param array    Array
     * @param <T>      Type
     */
    @SafeVarargs
    public static <T> void arrayForeach(final Consumer<T> consumer, final T... array)
    {
        Assert.notNull( array, consumer );

        Arrays.stream( array ).forEach( consumer );
    }

    /**
     * Perform an action for every item in array, given that the item matches a predicate
     * @param array Array
     * @param filter Predicate
     * @param consumer Consumer
     * @param <T> Type
     */
    public static <T> void arrayForeach(final T[] array, final Predicate<T> filter, final Consumer<T> consumer)
    {
        Assert.notNull( array, filter, consumer );

        Arrays.stream( array ).filter( filter ).forEach( consumer );
    }

}
