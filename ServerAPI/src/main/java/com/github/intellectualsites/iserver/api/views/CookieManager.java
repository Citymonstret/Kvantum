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
package com.github.intellectualsites.iserver.api.views;

import com.github.intellectualsites.iserver.api.request.Cookie;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * This is an utility class
 * created to handle all
 * cookie related actions
 *
 * @author Citymonstret
 */
final public class CookieManager
{

    private static final Map<String, Cookie> EMPTY_COOKIES = new HashMap<>( 0 );

    /**
     * Get all cookies from a HTTP Request
     *
     * @param r HTTP Request
     * @return an array containing the cookies
     */
    public static Map<String, Cookie> getCookies(final Request r)
    {
        Assert.isValid( r );

        String raw = r.getHeader( "Cookie" );

        if ( raw.isEmpty() )
        {
            return EMPTY_COOKIES;
        }

        raw = raw.replaceFirst( " ", "" );

        final String[] pieces = raw.split( "; " );
        final Map<String, Cookie> cookies = new HashMap<>( pieces.length );

        for ( final String piece : pieces )
        {
            final String[] subPieces = piece.split( "=" );
            if ( subPieces.length == 1 )
            {
                cookies.put( subPieces[ 0 ], new Cookie( subPieces[ 0 ], "" ) );
            } else
            {
                cookies.put( subPieces[ 0 ], new Cookie( subPieces[ 0 ], subPieces[ 1 ] ) );
            }
        }

        return cookies;
    }

}
