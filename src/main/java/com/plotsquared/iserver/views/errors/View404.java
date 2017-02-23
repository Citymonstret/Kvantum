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
package com.plotsquared.iserver.views.errors;

import com.plotsquared.iserver.core.CoreConfig;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class View404 extends Error
{

    private View404(final String url)
    {
        super( 404, "Not Found: " + url );
    }

    public static View404 construct(final String url)
    {
        final String webAddress = CoreConfig.webAddress.endsWith( "/" ) ?
                CoreConfig.webAddress.substring( 0, CoreConfig.webAddress.length() - 1 ) : CoreConfig.webAddress;
        return new View404( webAddress + url );
    }

}
