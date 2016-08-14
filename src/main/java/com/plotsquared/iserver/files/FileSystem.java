package com.plotsquared.iserver.files;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * A very simple (and restrictive) file system
 */
@SuppressWarnings( "unused" )
public class FileSystem
{

    final File coreFolder;
    private final Path corePath;

    /**
     * @param coreFolder The core folder (zero point) for this file system,
     *                   you cannot access any paths below it
     */
    public FileSystem(final File coreFolder)
    {
        this.coreFolder = coreFolder;
        this.corePath = new Path( this, "/", true );
    }

    /**
     * Get a path from a string, using the core folder as the parent
     * @param rawPath The raw path to the file
     * @return The created path
     * @throws IllegalPathException If the path tries to access a file outside of the allowed scope (such as ../)
     * @see #getPath(Path, String) To access files with a defined parent path
     */
    public Path getPath(final String rawPath) throws IllegalPathException
    {
        return this.getPath( corePath, rawPath );
    }

    /**
     * Get a path from a string
     * @param parent Parent path (folder), use {@link #getPath(String)} to access files within the core folder
     * @param rawPath The raw path to the file (relative to the parent)
     * @return The created path
     * @throws IllegalPathException If the path tries to access a file outside of the allowed scope (such as ../)
     */
    public Path getPath(final Path parent, String rawPath) throws IllegalPathException
    {
        final String[] parts = rawPath.split( "((?<=/)|(?=/))" );
        if ( parts.length < 1 )
        {
            return corePath;
        }
        if ( parts[0].equals( "." ) || parts[0].equals( "/" ) )
        {
            if ( parts.length >= 2 && parts[1].equals( "/" ) )
            {
                rawPath = rawPath.substring( 2 );
            } else
            {
                rawPath = rawPath.substring( 1 );
            }
        }
        for ( final String part : parts )
        {
            if ( StringUtils.countMatches( part, '.' ) > 1 )
            {
                throw new IllegalPathException( rawPath );
            }
        }
        final String lastPart = parts[parts.length - 1];
        return new Path( this, parent.toString() + rawPath, lastPart.indexOf( '.' ) == -1 );
    }

    public static final class IllegalPathException extends RuntimeException
    {

        IllegalPathException(final String path)
        {
            super( "Illegal path: \"" + path + "\"" );
        }

    }

}
