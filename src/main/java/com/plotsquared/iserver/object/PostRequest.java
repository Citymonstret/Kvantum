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
package com.plotsquared.iserver.object;

import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.RequestChild;
import com.plotsquared.iserver.util.StringUtil;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

final public class PostRequest implements RequestChild
{

    public final String request;
    private final Map<String, String> vars;
    private final Request parent;

    public PostRequest(final Request parent, final String request)
    {
        Assert.notNull( request );

        this.parent = parent;
        this.request = request;
        this.vars = new HashMap<>();
        for ( final String s : request.split( "&" ) )
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

    @Override
    public Request getParent()
    {
        return this.parent;
    }

    String buildLog()
    {
        return StringUtil.join( vars, "=", "&" );
    }

    public String get(final String k)
    {
        Assert.notNull( k );

        return vars.get( k );
    }

    public boolean contains(final String k)
    {
        Assert.notNull( k );

        return vars.containsKey( k );
    }

    public Map<String, String> get()
    {
        return new HashMap<>( this.vars );
    }

    public static PostRequest construct(final Request parent, final int cl, final BufferedReader input) throws Exception
    {
        final char[] chars = new char[ cl ];
        Assert.equals( input.read( chars ), cl );
        return new PostRequest( parent, new String( chars ) );
    }
}
