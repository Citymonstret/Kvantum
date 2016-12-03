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

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings( "unused" )
public class Path
{

    private final FileSystem fileSystem;
    private String path;
    private final boolean isFolder;
    // private final File file;
    private final java.nio.file.Path javaPath;

    private boolean exists;
    Map<String, Path> subPaths;

    Path(final FileSystem fileSystem, final String path, boolean isFolder)
    {
        this.fileSystem = fileSystem;
        if ( isFolder && !path.endsWith( "/" ) )
        {
            this.path = path + "/";
        } else
        {
            this.path = path;
        }
        if ( this.path.startsWith( "/" ) )
        {
            this.path = this.path.substring( 1 );
        }
        this.isFolder = isFolder;
        this.javaPath = fileSystem.coreFolder.resolve( this.path );
        this.exists = Files.exists( this.javaPath );
    }

    final public java.nio.file.Path getJavaPath()
    {
        return this.javaPath;
    }

    public final long length()
    {
        try
        {
            return Files.size( javaPath );
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
        return -1L;
    }

    final public byte[] readBytes()
    {
        if ( !exists )
        {
            return new byte[ 0 ];
        }
        byte[] content = new byte[0];
        if ( Files.isReadable( javaPath ) )
        {
            try
            {
                content = Files.readAllBytes( javaPath );
            } catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
        return content;
    }

    final public String readFile()
    {
        Logger.debug( "Trying to read file: " + toString() );

        final Optional<String> cacheEntry = Server.getInstance().getCacheManager().getCachedFile( toString() );
        if ( cacheEntry.isPresent() )
        {
            return cacheEntry.get();
        }
        if ( !exists )
        {
            return "";
        }
        final StringBuilder document = new StringBuilder();
        if ( Files.isReadable( javaPath ) )
        {
            try
            {
                Files.readAllLines( javaPath ).forEach( line -> document.append( line ).append( System.lineSeparator() ) );
            } catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
        final String content = document.toString();
        Server.getInstance().getCacheManager().setCachedFile( toString(), content );
        return content;
    }

    @Override
    final public String toString()
    {
        return this.path;
    }

    /**
     * @return true if the path target is a directory
     */
    public boolean isFolder()
    {
        return isFolder;
    }

    /**
     * Get a path relative to this
     * @param path Raw path
     * @return Relative path
     * @see FileSystem#getPath(Path, String)
     */
    public Path getPath(final String path)
    {
        return fileSystem.getPath( this, path );
    }

    Path getPathUnsafe(final String path)
    {
        return fileSystem.getPathUnsafe( this, path );
    }

    /**
     * Check if the file exists
     * @return true if the file exists
     */
    public boolean exists()
    {
        return this.exists;
    }

    /**
     * Create the file/directory, if it doesn't exist
     * <p>
     * Invokes {@link File#createNewFile()} if this path points to a file
     * </br>
     * Invokes {@link File#mkdirs()} if this path points to a directory
     * </p>
     * @return true if the file/directory was created
     */
    public boolean create()
    {
        if ( exists )
        {
            return false;
        }
        try
        {
            if ( isFolder )
            {
                return ( exists = Files.exists( Files.createDirectories( javaPath ) ) );
            }
            exists = Files.exists( Files.createFile( javaPath ) );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return exists;
    }

    public boolean isCached()
    {
        return Server.getInstance().getCacheManager().getCachedFile( toString() ).isPresent();
    }

    /**
     * Get the file extension
     * @return File extension, if a directory an empty string
     */
    public String getExtension()
    {
        if ( this.isFolder )
        {
            return "";
        }
        final String[] parts = this.path.split( "\\." );
        return parts[ parts.length - 1 ];
    }

    protected void loadSubPaths()
    {
        if ( !this.exists )
        {
            return;
        }
        if ( !this.isFolder )
        {
            this.subPaths = Collections.emptyMap();
            return;
        }
        try
        {
            final Stream<java.nio.file.Path> stream = Files.list( javaPath );
            final List<java.nio.file.Path> list = stream.collect( Collectors.toList() );
            this.subPaths = new HashMap<>();
            for ( final java.nio.file.Path p : list )
            {
                final Path path = getPathUnsafe( p.getFileName().toString() );
                this.subPaths.put( path.toString(), path );
            }
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Get all sub paths
     * @return Array containing the sub paths, will be empty if this isn't a directory
     * @see #isFolder() to check if this is a directory or not
     */
    public Collection<Path> getSubPaths() {
        return getSubPaths( true );
    }

    public Collection<Path> getSubPaths(boolean includeFolders)
    {
        if ( this.subPaths == null )
        {
            loadSubPaths();
        }
        if ( includeFolders )
        {
            return this.subPaths.values();
        }
        return subPaths.values().stream().filter( path1 -> !path1.isFolder ).collect( Collectors.toList() );
    }


    @SafeVarargs
    public final Collection<Path> getSubPaths(final Predicate<Path>... filters)
    {
        Stream<Path> stream = getSubPaths().stream();
        for ( final Predicate<Path> filter : filters )
        {
            stream = stream.filter( filter );
        }
        return stream.collect( Collectors.toList() );
    }

}
