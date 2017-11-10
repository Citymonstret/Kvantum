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

import java.util.Iterator;
import java.util.Map;

/**
 * Utility class with methods for dealing with strings
 */
@UtilityClass
public final class StringUtil
{

    /**
     * Join a map together into a string.
     * <pre>{@code
     * Map<String, String> map = new HashMap<>();
     * map.put("hello", "world");
     * map.put("goodbye", "new york");
     * String joined = join( map, " = ", "," );
     * // joined = "hello = world, goodbye = new york"
     * }</pre>
     * @param map Map to be joined
     * @param combiner String sequence used to combine the key and the value
     * @param separator String sequence used to separate map entries
     * @param <K> Key type
     * @param <V> Value type
     * @return joined string
     */
    public static <K, V> String join(final Map<K, V> map, final String combiner, final String separator)
    {
        Assert.notNull( map, combiner, separator );

        final StringBuilder builder = new StringBuilder();
        final Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        while ( iterator.hasNext() )
        {
            final Map.Entry<K, V> entry = iterator.next();
            builder.append( entry.getKey() ).append( combiner ).append( entry.getValue() );
            if ( iterator.hasNext() )
            {
                builder.append( separator );
            }
        }
        return builder.toString();
    }

}
