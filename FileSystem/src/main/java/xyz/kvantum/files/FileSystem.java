/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.files;

/**
 * A very simple (and restrictive) file system
 */
@SuppressWarnings({ "unused", "WeakerAccess" })
public class FileSystem
{

    final java.nio.file.Path coreFolder;
    private final Path corePath;
    private final FileCacheManager fileCacheManager;

    /**
     * @param coreFolder The core folder (zero point) for this file system,
     *                   you cannot access any paths below it
     * @param fileCacheManager Cache manager implementation
     */
    public FileSystem(final java.nio.file.Path coreFolder, final FileCacheManager fileCacheManager)
    {
        if ( coreFolder == null )
        {
            throw new NullPointerException( "folder was null" );
        }
        if ( fileCacheManager == null )
        {
            throw new NullPointerException( "cache manager was null" );
        }
        this.coreFolder = coreFolder;
        this.fileCacheManager = fileCacheManager;
        this.corePath = new Path( this, "/", true );
    }

    public FileCacheManager getFileCacheManager()
    {
        return fileCacheManager;
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

    Path getPathUnsafe(final Path parent, final String rawPath)
    {
        final String[] parts = rawPath.split( "((?<=/)|(?=/))" );
        if ( parts.length < 1 )
        {
            return corePath;
        }
        String finalPath = rawPath;
        if ( parts[ 0 ].equals( "." ) || parts[ 0 ].equals( "/" ) )
        {
            if ( parts.length >= 2 && parts[ 1 ].equals( "/" ) )
            {
                finalPath = rawPath.substring( 2 );
            } else
            {
                finalPath = rawPath.substring( 1 );
            }
        }
        final String lastPart = parts[ parts.length - 1 ];
        return new Path( this, parent.toString() + finalPath, lastPart.indexOf( '.' ) == -1 );
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
        if ( rawPath == null )
        {
            throw new NullPointerException( "Path was null..." );
        }
        final String[] parts = rawPath.split( "((?<=/)|(?=/))" );
        if ( parts.length < 1 )
        {
            return corePath;
        }
        if ( parts[ 0 ].equals( "." ) || parts[ 0 ].equals( "/" ) )
        {
            if ( parts.length >= 2 && parts[ 1 ].equals( "/" ) )
            {
                rawPath = rawPath.substring( 2 );
            } else
            {
                rawPath = rawPath.substring( 1 );
            }
        }
        for ( final String part : parts )
        {
            if ( part.contains( ".." ) )
            {
                throw new IllegalPathException( rawPath );
            }
        }
        final String lastPart = parts[ parts.length - 1 ];
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

    /**
     * Exception thrown when an attempt to access an illegal path in {@link FileSystem}
     * or {@link Path} has been made
     */
    public static final class IllegalPathException extends RuntimeException
    {

        IllegalPathException(final String path)
        {
            super( "Illegal path: \"" + path + "\"" );
        }

    }

}
