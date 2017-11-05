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
package com.github.intellectualsites.iserver.files;

import java.util.Optional;

@SuppressWarnings("unused")
public enum Extension
{
    CSS( new String[]{ "css" } ),
    LESS( new String[]{ "less", "css" } ),
    HTML( new String[]{ "html", "xhtml", "htm", "vm" } ),
    PNG( new String[]{ "png" } ),
    ICO( new String[]{ "ico" } ),
    GIF( new String[]{ "gif" } ),
    JPEG( new String[]{ "jpg", "jpeg" } ),
    ZIP( new String[]{ "zip" } ),
    TXT( new String[]{ "txt" } ),
    PDF( new String[]{ "pdf" } ),
    JAVASCRIPT( new String[]{ "js", } );

    private final String[] extensions;

    Extension(final String[] extensions)
    {
        this.extensions = extensions;
    }

    public static Optional<Extension> getExtension(final String string)
    {
        String workingString = string;
        if ( string.startsWith( "." ) )
        {
            workingString = string.substring( 1 );
        }
        for ( final Extension extension : values() )
        {
            for ( final String e : extension.extensions )
            {
                if ( e.equalsIgnoreCase( workingString ) )
                {
                    return Optional.of( extension );
                }
            }
        }
        return Optional.empty();
    }

    public String[] getExtensions()
    {
        return this.extensions;
    }

    public boolean matches(final String string)
    {
        String workingString = string;
        if ( workingString.startsWith( "." ) )
        {
            workingString = workingString.substring( 1 );
        }
        for ( final String e : extensions )
        {
            if ( e.equalsIgnoreCase( workingString ) )
            {
                return true;
            }
        }
        return false;
    }

}
