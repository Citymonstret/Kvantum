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

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.request.Cookie;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is an utility class
 * created to handle all
 * cookie related actions
 *
 * @author Citymonstret
 */
final public class CookieManager
{

    private static final Pattern PATTERN_COOKIE = Pattern.compile( "(?<key>[A-Za-z0-9_\\-]*)=" +
            "(?<value>.*)?" );
    private static final ListMultimap<String, Cookie> EMPTY_COOKIES = ArrayListMultimap.create( 0, 0 );

    /**
     * Get all cookies from a HTTP Request
     *
     * @param r HTTP Request
     * @return an array containing the cookies
     */
    public static ListMultimap<String, Cookie> getCookies(final AbstractRequest r)
    {
        Assert.isValid( r );

        String raw = r.getHeader( "Cookie" ).replaceAll( "\\s", "" );

        if ( raw.isEmpty() )
        {
            return EMPTY_COOKIES;
        }

        final ListMultimap<String, Cookie> cookies = MultimapBuilder.hashKeys().arrayListValues()
                .build();

        final StringTokenizer cookieTokenizer = new StringTokenizer( raw, ";" );
        while ( cookieTokenizer.hasMoreTokens() )
        {
            final String cookieString = cookieTokenizer.nextToken();

            final Matcher matcher = PATTERN_COOKIE.matcher( cookieString );
            if ( matcher.matches() )
            {
                if ( matcher.groupCount() < 2 )
                {
                    cookies.put( matcher.group( "key" ), new Cookie( matcher.group( "key" ), "" ) );
                } else
                {
                    cookies.put( matcher.group( "key" ), new Cookie( matcher.group( "key" ),
                            matcher.group( "value" ) ) );
                }
            }
        }

        return cookies;
    }

}
