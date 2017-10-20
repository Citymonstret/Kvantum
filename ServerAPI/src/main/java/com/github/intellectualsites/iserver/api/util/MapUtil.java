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

import java.util.HashMap;
import java.util.Map;

public final class MapUtil
{

    public static <I, O> Map<String, O> convertMap(final Map<String, I> input, final Converter<I, O>
            converter)
    {
        final Map<String, O> output = new HashMap<>();
        input.forEach( (key, value) -> output.put( key, converter.convert( value ) ) );
        return output;
    }

    public interface Converter<I, O>
    {

        O convert(I input);

    }

}
