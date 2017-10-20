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

import java.util.Iterator;
import java.util.Map;

@UtilityClass
public final class StringUtil
{

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
