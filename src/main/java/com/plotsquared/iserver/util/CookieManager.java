/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
package com.plotsquared.iserver.util;

import com.plotsquared.iserver.object.Cookie;
import com.plotsquared.iserver.object.Request;

/**
 * This is an utility class
 * created to handle all
 * cookie related actions
 *
 * @author Citymonstret
 */
public class CookieManager
{

    /**
     * Get all cookies from a HTTP Request
     *
     * @param r HTTP Request
     * @return an array containing the cookies
     */
    public static Cookie[] getCookies(final Request r)
    {
        Assert.isValid( r );

        String raw = r.getHeader( "Cookie" );
        if ( raw.equals( "" ) )
        {
            return new Cookie[ 0 ];
        }
        raw = raw.replaceFirst( " ", "" );
        final String[] pieces = raw.split( "; " );
        final Cookie[] cookies = new Cookie[ pieces.length ];
        for ( int i = 0; i < pieces.length; i++ )
        {
            final String piece = pieces[ i ];
            final String[] piecePieces = piece.split( "=" );
            if ( piecePieces.length == 1 )
            {
                cookies[ i ] = new Cookie( piecePieces[ 0 ], "" );
            } else
            {
                cookies[ i ] = new Cookie( piecePieces[ 0 ], piecePieces[ 1 ] );
            }
        }
        return cookies;
    }

}
