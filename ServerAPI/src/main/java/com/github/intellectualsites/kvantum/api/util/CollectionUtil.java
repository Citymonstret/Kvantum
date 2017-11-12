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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for common {@link Collection collection} operations
 */
@UtilityClass
@SuppressWarnings({ "unused", "WeakerAccess" })
public final class CollectionUtil
{

    /**
     * Clear a collection and return the amount of cleared objects
     *
     * @param collection Collection to be cleared
     * @return Size of collection before clearing
     */
    public static int clear(final Collection collection)
    {
        final int size = collection.size();
        collection.clear();
        return size;
    }

    /**
     * Convert a list to a string list using {@link Object#toString()}
     *
     * @param collection Collection to be converted
     * @param <T>        Type
     * @return list of strings
     */
    public static <T> List<String> toStringList(final Collection<T> collection)
    {
        final List<String> returnList = new ArrayList<>( collection.size() );
        collection.forEach( o -> returnList.add( o.toString() ) );
        return returnList;
    }

    /**
     * Join all items in a list into a string, using a set delimiter
     *
     * @param collection      Collection to be joined
     * @param stringGenerator String generator
     * @param joiner          String that will be used to join the items
     * @param <T>             Type
     * @return Joined string
     * @see #join(Collection, String) for an {@link Object#toString()} implementation
     */
    public static <T> String smartJoin(final Collection<T> collection, final Generator<T, String> stringGenerator,
                                       final String joiner)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        final Iterator<T> iterator = collection.iterator();
        while ( iterator.hasNext() )
        {
            stringBuilder.append( stringGenerator.generate( iterator.next() ) );
            if ( iterator.hasNext() )
            {
                stringBuilder.append( joiner );
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Join all items in a list into a string, using a set delimiter and {@link Object#toString()}
     * as the string generator.
     *
     * @param collection Collection to be joined
     * @param joiner     String that will be used to join the items
     * @param <T>        Type
     * @return Joined string
     * @see #smartJoin(Collection, Generator, String) to customize the string generation behavior
     */
    public static <T> String join(final Collection<T> collection, String joiner)
    {
        return smartJoin( collection, Object::toString, joiner );
    }

    /**
     * Check if a string collection contains a string, ignoring string casing
     * @param collection Collecting
     * @param string String
     * @return true if the collection contains the string, regardless of casing
     */
    public static boolean containsIgnoreCase(final Collection<? extends String> collection, final String string)
    {
        Assert.notNull( collection );
        Assert.notNull( string );

        if ( collection.isEmpty() )
        {
            return false;
        }
        for ( final String entry : collection )
        {
            if ( entry.equalsIgnoreCase( string ) )
            {
                return true;
            }
        }
        return false;
    }

}
