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
import com.plotsquared.iserver.util.LambdaUtil;

import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public enum FileType
{
    HTML( "html", Header.CONTENT_TYPE_HTML ),
    CSS( "css", Header.CONTENT_TYPE_CSS ),
    JAVASCRIPT( "js", Header.CONTENT_TYPE_JAVASCRIPT ),
    LESS( "less", Header.CONTENT_TYPE_CSS );

    private final String extension;

    private final String contentType;

    FileType(String extension, String contentType)
    {
        this.extension = extension;
        this.contentType = contentType;
    }

    public static Optional<FileType> byExtension(final String ext)
    {
        Assert.notNull( ext );

        final Predicate<FileType> filter = type -> type.extension.equalsIgnoreCase( ext );
        return LambdaUtil.getFirst( values(), filter );
    }

    public String getExtension()
    {
        return extension;
    }

    public String getContentType()
    {
        return contentType;
    }

}
