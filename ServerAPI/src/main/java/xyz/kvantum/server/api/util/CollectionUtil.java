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
