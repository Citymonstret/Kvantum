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
package com.github.intellectualsites.kvantum.api.request.post;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.request.Request;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

final public class UrlEncodedPostRequest extends PostRequest
{

    public UrlEncodedPostRequest(final Request parent, final String request)
    {
        super( parent, request, false );
    }

    @Override
    protected void parseRequest(final String request)
    {
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
        this.setRequest( fixedRequest );
        for ( final String s : fixedRequest.split( "&" ) )
        {
            if ( !s.isEmpty() )
            {
                final String[] p = s.split( "=" );
                if ( p.length < 2 )
                {
                    continue;
                }
                getVariables().put( p[ 0 ], p[ 1 ].replace( "+", " " ) );
            }
        }
    }

    @Override
    public EntityType getEntityType()
    {
        return EntityType.FORM_URLENCODED;
    }

}
