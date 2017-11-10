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

import java.util.Locale;
import java.util.Optional;

/**
 * Protocol implementation enum
 */
public enum ProtocolType
{

    HTTP,
    HTTPS;

    /**
     * Match a string to a {@link ProtocolType}, if possible
     * @param string String to match, may not be null
     * @return matched protocol type if found
     */
    public static Optional<ProtocolType> getByName(final String string)
    {
        Assert.notEmpty( string );

        final String fixed = string.replaceAll( "\\s", "" ).toUpperCase( Locale.ENGLISH );
        return LambdaUtil.getFirst( values(), type -> type.name().equals( fixed ) );
    }

}
