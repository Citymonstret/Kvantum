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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for dealing with common {@link Map} operations
 */
public final class MapUtil
{

    /**
     * Convert a map with a certain type to another type, given that the keys are Strings
     *
     * @param input     Inputted map
     * @param converter Converter, that will convert the type of the inputted map
     *                  into the output type
     * @param <I>       Input type
     * @param <O>       Output type
     * @return Converted map
     */
    public static <I, O> Map<String, O> convertMap(final Map<String, I> input, final Converter<I, O>
            converter)
    {
        final Map<String, O> output = new HashMap<>();
        input.forEach( (key, value) -> output.put( key, converter.convert( value ) ) );
        return output;
    }

    /**
     * Join a map together into a string.
     * <pre>{@code
     * Map<String, String> map = new HashMap<>();
     * map.put("hello", "world");
     * map.put("goodbye", "new york");
     * String joined = join( map, " = ", "," );
     * // joined = "hello = world, goodbye = new york"
     * }</pre>
     *
     * @param map       Map to be joined
     * @param combiner  String sequence used to combine the key and the value
     * @param separator String sequence used to separate map entries
     * @param <K>       Key type
     * @param <V>       Value type
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

    /**
     * Helper interface for {@link #convertMap(Map, Converter)}
     *
     * @param <I> Input type
     * @param <O> Output type
     */
    @FunctionalInterface
    public interface Converter<I, O>
    {

        /**
         * Convert an input to another type
         *
         * @param input Input to be converted
         * @return Converted object
         */
        O convert(I input);

    }

}
