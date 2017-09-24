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
package com.plotsquared.iserver.api.request;

import com.plotsquared.iserver.api.util.Assert;
import com.plotsquared.iserver.api.util.LambdaUtil;

import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("ALL")
public enum HttpMethod
{

    /**
     * Post requests are used
     * to handle data
     */
    POST,

    /**
     * Get requests are handled
     * for getting resources
     */
    GET,

    /**
     *
     */
    PUT,

    /**
     *
     */
    HEAD( false ),

    /**
     *
     */
    DELETE;

    private final boolean hasBody;

    HttpMethod(final boolean hasBody)
    {
        this.hasBody = hasBody;
    }

    HttpMethod()
    {
        this( true );
    }

    public static Optional<HttpMethod> getByName(final String name)
    {
        Assert.notEmpty( name );

        final String fixed = name.replaceAll( "\\s", "" ).toUpperCase( Locale.ENGLISH );
        return LambdaUtil.getFirst( values(), method -> method.name().equals( fixed ) );
    }

    public boolean hasBody()
    {
        return this.hasBody;
    }
}
