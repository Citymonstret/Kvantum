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