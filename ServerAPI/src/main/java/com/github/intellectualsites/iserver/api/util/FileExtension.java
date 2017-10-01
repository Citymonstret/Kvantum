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
package com.github.intellectualsites.iserver.api.util;

import com.github.intellectualsites.iserver.api.response.Header;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum FileExtension
{
    CSS( new String[]{ "css", }, Header.CONTENT_TYPE_CSS, "/* {cmt} */" ),
    LESS( new String[]{ "less", "css" }, Header.CONTENT_TYPE_CSS, "/* {cmt} */"),
    HTML( new String[]{ "html", "xhtml", "htm" }, Header.CONTENT_TYPE_HTML, "<!-- {cmt} -->"),
    PNG( new String[]{ "png" }, "image/png; charset=utf-8", "png", ReadType.BYTES, ""),
    ICO( new String[]{ "ico" }, "image/x-icon; charset=utf-8", "x-icon", ReadType.BYTES, ""),
    GIF( new String[]{ "gif" }, "image/gif; charset=utf-8", "gif", ReadType.BYTES, ""),
    JPEG( new String[]{ "jpg", "jpeg" }, "image/jpeg; charset=utf-8", "jpeg", ReadType.BYTES, ""),
    ZIP( new String[]{ "zip" }, Header.CONTENT_TYPE_OCTET_STREAM, "zip", ReadType.BYTES, ""),
    TXT( new String[]{ "txt" }, Header.CONTENT_TYPE_OCTET_STREAM, "txt", ReadType.BYTES, ""),
    PDF( new String[]{ "pdf" }, Header.CONTENT_TYPE_OCTET_STREAM, "pdf", ReadType.BYTES, ""),
    JAVASCRIPT( new String[]{ "js", }, Header.CONTENT_TYPE_JAVASCRIPT, "/* {cmt} */");

    public static final List<FileExtension> IMAGE = Collections.unmodifiableList( Arrays.asList( PNG, ICO, GIF, JPEG ) );
    public static final List<FileExtension> DOWNLOADABLE = Collections.unmodifiableList( Arrays.asList( PDF, TXT, ZIP
    ) );
    private final String option;
    private final String[] extensions;
    private final String contentType;
    private final ReadType readType;
    private final String comment;

    FileExtension(final String[] extensions, final String contentType, final String comment)
    {
        this.extensions = extensions;
        this.contentType = contentType;
        this.option = "";
        this.readType = ReadType.TEXT;
        this.comment = comment;
    }

    FileExtension(final String[] extensions, final String contentType, final String option, final ReadType readType,
                  final String comment)
    {
        this.extensions = extensions;
        this.contentType = contentType;
        this.option = option;
        this.readType = readType;
        this.comment = comment;
    }

    public static Optional<FileExtension> getExtension(String string)
    {
        if ( string.startsWith( "." ) )
        {
            string = string.substring( 1 );
        }
        for ( final FileExtension extension : values() )
        {
            for ( final String e : extension.extensions )
            {
                if ( e.equalsIgnoreCase( string ) )
                {
                    return Optional.of( extension );
                }
            }
        }
        return Optional.empty();
    }

    public String getComment(final String comment)
    {
        return this.comment.replace( "{cmt}", comment );
    }

    public ReadType getReadType()
    {
        return readType;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getOption()
    {
        return option;
    }

    public boolean isImage()
    {
        return IMAGE.contains( this );
    }

    public boolean matches(String string)
    {
        if ( string.startsWith( "." ) )
        {
            string = string.substring( 1 );
        }
        for ( final String e : extensions )
        {
            if ( e.equalsIgnoreCase( string ) )
            {
                return true;
            }
        }
        return false;
    }

    public String getExtension()
    {
        return extensions[ 0 ];
    }

    public enum ReadType
    {
        TEXT, BYTES
    }
}
