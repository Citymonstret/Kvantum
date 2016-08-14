package com.plotsquared.iserver.files;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class FileSystem
{

    final File coreFolder;
    final Path corePath;

    public FileSystem(final File coreFolder)
    {
        this.coreFolder = coreFolder;
        this.corePath = new Path( this, "/", true );
    }

    public Path getPath(final String name) throws IllegalPathException, PathNotFoundException
    {
        return this.getPath( corePath, name );
    }

    public Path getPath(final Path parent, String name) throws IllegalPathException, PathNotFoundException
    {
        final String[] parts = name.split( "((?<=/)|(?=/))" );
        if ( parts.length < 1 )
        {
            return corePath;
        }
        if ( parts[0].equals( "." ) || parts[0].equals( "/" ) )
        {
            if ( parts.length >= 2 && parts[1].equals( "/" ) )
            {
                name = name.substring( 2 );
            } else
            {
                name = name.substring( 1 );
            }
        }
        for ( final String part : parts )
        {
            if ( StringUtils.countMatches( part, '.' ) > 1 )
            {
                throw new IllegalPathException( name );
            }
        }
        final String lastPart = parts[parts.length - 1];
        final Path path = new Path( this, parent.toString() + name, lastPart.indexOf( '.' ) == -1 );
        if ( !path.getFile().exists() )
        {
            throw new PathNotFoundException( path.toString() );
        }
        return path;
    }

    public static final class PathNotFoundException extends RuntimeException
    {
        PathNotFoundException(final String path)
        {
            super( "Could not find path: \"" + path + "\"" );
        }
    }

    public static final class IllegalPathException extends RuntimeException
    {

        IllegalPathException(final String path)
        {
            super( "Illegal path: \"" + path + "\"" );
        }

    }

}
