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

import java.util.Collection;
import java.util.Locale;

public final class CollectionUtil
{

    public static int clear(final Collection collection)
    {
        final int size = collection.size();
        collection.clear();
        return size;
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
