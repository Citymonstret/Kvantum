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
package com.github.intellectualsites.iserver.api.response;

import com.github.intellectualsites.iserver.api.util.Assert;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@EqualsAndHashCode(of = "text")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderOption
{

    private static Map<String, HeaderOption> headerOptionMap = new HashMap<>();

    @NonNull
    @Getter
    private final String text;
    @Getter
    private boolean cacheApplicable = true;

    public static HeaderOption create(final String text)
    {
        return create( text, true );
    }

    public static HeaderOption create(final String text, boolean cacheApplicable)
    {
        final HeaderOption headerOption = new HeaderOption( Assert.notNull( text ) ).cacheApplicable( cacheApplicable );
        headerOptionMap.put( text.toLowerCase(), headerOption );
        return headerOption;
    }

    public static HeaderOption getOrCreate(final String text)
    {
        if ( headerOptionMap.containsKey( text.toLowerCase() ) )
        {
            return headerOptionMap.get( text.toLowerCase() );
        }
        return create( text );
    }

    private HeaderOption cacheApplicable(final boolean b)
    {
        this.cacheApplicable = b;
        return this;
    }

    @Override
    public final String toString()
    {
        return this.text;
    }

}
