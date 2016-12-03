package com.plotsquared.iserver.util;

import com.plotsquared.iserver.object.Header;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum FileExtension
{
    CSS(new String[] { "css", }, Header.CONTENT_TYPE_CSS),
    LESS(new String[] { "less", "css" }, Header.CONTENT_TYPE_CSS),
    HTML(new String[] { "html", "xhtml", "htm" }, Header.CONTENT_TYPE_HTML),
    PNG(new String[] { "png" }, "image/png; charset=utf-8", "png", ReadType.BYTES),
    ICO(new String[] { "ico" }, "image/x-icon; charset=utf-8", "x-icon", ReadType.BYTES),
    GIF(new String[] { "gif" }, "image/gif; charset=utf-8", "gif", ReadType.BYTES),
    JPEG(new String[] { "jpg", "jpeg" }, "image/jpeg; charset=utf-8", "jpeg", ReadType.BYTES),
    ZIP(new String[] { "zip" }, Header.CONTENT_TYPE_OCTET_STREAM, "zip", ReadType.BYTES),
    TXT(new String[] { "txt" }, Header.CONTENT_TYPE_OCTET_STREAM, "txt", ReadType.BYTES),
    PDF(new String[] { "pdf" }, Header.CONTENT_TYPE_OCTET_STREAM, "pdf", ReadType.BYTES),
    JAVASCRIPT(new String[] { "js", }, Header.CONTENT_TYPE_JAVASCRIPT);

    private final String option;
    private final String[] extensions;
    private final String contentType;
    private final ReadType readType;

    FileExtension(final String[] extensions, final String contentType) {
        this.extensions = extensions;
        this.contentType = contentType;
        this.option = "";
        this.readType = ReadType.TEXT;
    }

    FileExtension(final String[] extensions, final String contentType, final String option, final ReadType readType)
    {
        this.extensions = extensions;
        this.contentType = contentType;
        this.option = option;
        this.readType = readType;
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

    public static final List<FileExtension> IMAGE = Collections.unmodifiableList(Arrays.asList( PNG, ICO, GIF, JPEG ) );

    public static final List<FileExtension> DOWNLOADABLE = Collections.unmodifiableList( Arrays.asList( PDF, TXT, ZIP
    ) );

    public boolean isImage()
    {
        return IMAGE.contains( this );
    }

    public boolean matches( String string )
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

    public String getExtension()
    {
        return extensions[ 0 ];
    }

    public enum ReadType
    {
        TEXT, BYTES;
    }
}
