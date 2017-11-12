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
package com.github.intellectualsites.kvantum.api.request;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.MapUtil;
import lombok.Getter;

import java.io.BufferedReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

final public class PostRequest implements RequestChild
{

    /*
    TODO: Support more POST request body types, such as:
    - JSON
    - Xml
    - Multipart forms
     */

    public final String request;
    private final Map<String, String> vars;
    @Getter
    private final Request parent;

    PostRequest(final Request parent, final String request)
    {
        Assert.notNull( request );
        this.parent = parent;
        String fixedRequest;
        try
        {
            fixedRequest = URLDecoder.decode( request, StandardCharsets.UTF_8.toString() );
        } catch ( final Exception e )
        {
            if ( CoreConfig.debug )
            {
                e.printStackTrace();
            }
            fixedRequest = request;
        }
        this.request = fixedRequest;
        this.vars = new HashMap<>();
        for ( final String s : this.request.split( "&" ) )
        {
            if ( !s.isEmpty() )
            {
                final String[] p = s.split( "=" );
                if ( p.length < 2 )
                {
                    continue;
                }
                vars.put( p[ 0 ], p[ 1 ].replace( "+", " " ) );
            }
        }
    }

    /**
     * Construct a new post request
     *
     * @param parent Parent request
     * @param cl     Content-Length (header value)
     * @param input  Reader from which the request is to be read
     * @return constructed request
     * @throws Exception If the reader fails to construct a request
     */
    public static PostRequest construct(final Request parent, final int cl, final BufferedReader input) throws Exception
    {
        final char[] chars = new char[ cl ];
        Assert.equals( input.read( chars ), cl );
        return new PostRequest( parent, new String( chars ) );
    }

    String buildLog()
    {
        return MapUtil.join( vars, "=", "&" );
    }

    /**
     * Get a parameter
     * @param k Parameter key
     * @return Parameter value
     */
    public String get(final String k)
    {
        Assert.notNull( k );

        return vars.get( k );
    }

    /**
     * Check if a parameter is stored
     * @param k Parameter key
     * @return True of the parameter is stored; else null
     */
    public boolean contains(final String k)
    {
        Assert.notNull( k );

        return vars.containsKey( k );
    }

    /**
     * Get a copy of the internal parameter map
     * @return copy of the internal map
     */
    public Map<String, String> get()
    {
        return new HashMap<>( this.vars );
    }
}
