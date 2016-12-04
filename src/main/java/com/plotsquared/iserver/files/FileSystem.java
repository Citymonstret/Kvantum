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
package com.plotsquared.iserver.files;

import org.apache.commons.lang3.StringUtils;

/**
 * A very simple (and restrictive) file system
 */
@SuppressWarnings( "unused" )
public final class FileSystem
{

    final java.nio.file.Path coreFolder;
    private final Path corePath;

    /**
     * @param coreFolder The core folder (zero point) for this file system,
     *                   you cannot access any paths below it
     */
    public FileSystem(final java.nio.file.Path coreFolder)
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
        if ( rawPath == null || rawPath.isEmpty() )
        {
            return this.corePath;
        }
        return this.getPath( corePath, rawPath );
    }

    Path getPathUnsafe(final Path parent, String rawPath)
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
        final String lastPart = parts[parts.length - 1];
        return new Path( this, parent.toString() + rawPath, lastPart.indexOf( '.' ) == -1 );
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
        if ( parent.subPaths == null )
        {
            parent.loadSubPaths();
        }
        if ( parent.subPaths.containsKey( rawPath ) )
        {
            return parent.subPaths.get( rawPath );
        }
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
