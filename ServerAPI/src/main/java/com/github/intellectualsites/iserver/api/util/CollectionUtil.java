/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver.api.util;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
@SuppressWarnings({ "unused", "WeakerAccess" })
public final class CollectionUtil
{

    public static int clear(final Collection collection)
    {
        final int size = collection.size();
        collection.clear();
        return size;
    }

    public static <T> List<String> toStringList(final Collection<T> collection)
    {
        final List<String> returnList = new ArrayList<>( collection.size() );
        collection.forEach( o -> returnList.add( o.toString() ) );
        return returnList;
    }

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

    public static <T> String join(final Collection<T> collection, String joiner)
    {
        return smartJoin( collection, Object::toString, joiner );
    }

    public static boolean containsIgnoreCase(final Collection<? extends String> collection, String string)
    {
        Assert.notNull( collection );
        Assert.notNull( string );

        if ( collection.isEmpty() )
        {
            return false;
        }
        string = string.toLowerCase( Locale.ENGLISH );
        for ( final String entry : collection )
        {
            if ( entry.equals( string ) )
            {
                return true;
            }
        }
        return false;
    }

}
