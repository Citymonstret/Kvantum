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
package com.plotsquared.iserver.api.response;

import com.plotsquared.iserver.api.util.Assert;

public final class HeaderOption
{

    private final String text;
    private boolean cacheApplicable = true;

    private HeaderOption(final String text)
    {
        Assert.notNull( text );

        this.text = text;
    }

    public static HeaderOption create(final String text)
    {
        return new HeaderOption( text );
    }

    public static HeaderOption create(final String text, boolean cacheApplicable)
    {
        return new HeaderOption( text ).cacheApplicable( cacheApplicable );
    }

    public String getText()
    {
        return text;
    }

    public boolean isCacheApplicable()
    {
        return cacheApplicable;
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
