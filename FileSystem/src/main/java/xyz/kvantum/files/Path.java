/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({ "unused", "WeakerAccess" }) public class Path
{

	private final FileSystem fileSystem;
	private final boolean isFolder;
	// private final File file;
	private final java.nio.file.Path javaPath;
	Map<String, Path> subPaths;
	private String path;
	private boolean exists;
	private BasicFileAttributes basicFileAttributes;
	private FileWatcher.FileWatchingContext fileWatchingContext;

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

	private static String replaceName(final String in)
	{
		return in.replaceAll( "§", "\\." );
	}

	/**
	 * Get the java nio version of this path
	 *
	 * @return {@link Path} version of this path
	 */
	final public java.nio.file.Path getJavaPath()
	{
		return this.javaPath;
	}

	/**
	 * Calculate the size of the file (or files, if the current path is a directory)
	 *
	 * @return file(s) size | -1L if anything goes wrong
	 */
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

	private void loadAttributes() throws Exception
	{
		if ( this.basicFileAttributes != null )
		{
			return;
		}
		this.basicFileAttributes = Files.readAttributes( javaPath, BasicFileAttributes.class );
	}

	public FileWatcher.FileWatchingContext getFileWatchingContext()
	{
		return this.fileWatchingContext;
	}

	public void registerWatcher(final FileWatcher fileWatcher, final BiConsumer<Path, WatchEvent.Kind<?>> reaction)
			throws IOException
	{
		if ( !this.isFolder() )
		{
			throw new UnsupportedOperationException( "Cannot register a watcher for a file" );
		}
		this.fileWatchingContext = fileWatcher.registerPath( this, reaction );
	}

	/**
	 * Get the file creation time in milliseconds
	 *
	 * @return file creation time
	 */
	public long getCreationTime()
	{
		try
		{
			this.loadAttributes();
			return this.basicFileAttributes.creationTime().toMillis();
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
		return -1;
	}

	public long getLastModified()
	{
		try
		{
			this.loadAttributes();
			return this.basicFileAttributes.lastModifiedTime().toMillis();
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Removes the stored sub paths
	 */
	public void invalidateSubPaths()
	{
		this.subPaths = null;
	}

	/**
	 * Attempt to read the file that the path is pointing to
	 *
	 * @return if the file is readable; the file content | if the file isn't readable; an empty byte array
	 */
	final public byte[] readBytes()
	{
		if ( !exists )
		{
			return new byte[ 0 ];
		}
		final Optional<CachedFile> cacheEntry = fileSystem.getFileCacheManager().readCachedFile( this );
		if ( cacheEntry.isPresent() )
		{
			return cacheEntry.get().getAsByteArray();
		}
		byte[] content = new byte[ 0 ];
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
		fileSystem.getFileCacheManager().writeCachedFile( this, new CachedFile( content ) );
		return content;
	}

	/**
	 * Attempt to read the file that the path is pointing to. Will first check the file cache, then attempt to read the
	 * file
	 *
	 * @return if the file is readable; the file content | if the file isn't readable; an empty string
	 */
	final public String readFile()
	{
		if ( !exists )
		{
			return "";
		}
		final Optional<CachedFile> cacheEntry = fileSystem.getFileCacheManager().readCachedFile( this );
		if ( cacheEntry.isPresent() )
		{
			return cacheEntry.get().getAsString();
		}
		final StringBuilder document = new StringBuilder();
		if ( Files.isReadable( javaPath ) )
		{
			try
			{
				Files.readAllLines( javaPath )
						.forEach( line -> document.append( line ).append( System.lineSeparator() ) );
			} catch ( IOException e )
			{
				e.printStackTrace();
			}
		}
		final String content = document.toString();
		fileSystem.getFileCacheManager().writeCachedFile( this, new CachedFile( content ) );
		return content;
	}

	@Override final public String toString()
	{
		return this.path;
	}

	public String getEntityName()
	{
		final String name = this.path;
		final String[] pathTokens = name.split( "/" );
		String entityToken = pathTokens[ pathTokens.length - 1 ];
		if ( entityToken.contains( "." ) )
		{
			entityToken = entityToken.split( "\\." )[ 0 ];
		}
		return entityToken;
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
	 *
	 * @param path Raw path
	 * @return Relative path
	 * @see FileSystem#getPath(Path, String)
	 */
	public Path getPath(final String path)
	{
		return fileSystem.getPath( this, replaceName( path ) );
	}

	public Path forcePath(final String path)
	{
		final Path subPath = getPath( path );
		if ( !subPath.exists() )
		{
			subPath.create();
		}
		return subPath;
	}

	public Path getPath(String path, final Extension extension)
	{
		path = replaceName( path );
		final Collection<Path> subPaths = getSubPaths();

		for ( final Path subPath : subPaths )
		{
			if ( subPath.getEntityName().equalsIgnoreCase( path ) )
			{
				for ( final String ext : extension.getExtensions() )
				{
					if ( subPath.getExtension().equalsIgnoreCase( ext ) )
					{
						return subPath;
					}
				}
			}
		}
		return getPath( path + "." + extension.getExtensions()[ 0 ] );
	}

	private Path getPathUnsafe(final String path)
	{
		return fileSystem.getPathUnsafe( this, replaceName( path ) );
	}

	/**
	 * Check if the file exists
	 *
	 * @return true if the file exists
	 */
	public boolean exists()
	{
		return this.exists;
	}

	/**
	 * Create the file/directory, if it doesn't exist <p> Invokes {@link Files#createFile(java.nio.file.Path, * FileAttribute[]) Files.createFile} if this path points to a file; Invokes {@link
	 * Files#createDirectories(java.nio.file.Path, FileAttribute[]) Files.createDirectories} if this path points to a
	 * directory </p>
	 *
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

	/**
	 * See if the file is stored in the file cache
	 *
	 * @return true if the file is stored in the cache | else false
	 */
	public boolean isCached()
	{
		return fileSystem.getFileCacheManager().readCachedFile( this ).isPresent();
	}

	/**
	 * Get the file extension
	 *
	 * @return if a file; file extension | if a directory; an empty string
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
		} catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	public boolean copy(final Path targetDirectory)
	{
		final Path target = targetDirectory.forcePath( this.getEntityName() + "." + this.getExtension() );
		if ( this.isFolder() )
		{
			for ( final Path subPath : getSubPaths() )
			{
				final boolean result = subPath.copy( targetDirectory );
				if ( !result )
				{
					return false;
				}
			}
		} else
		{
			try
			{
				Files.copy( this.getJavaPath(), target.javaPath, StandardCopyOption.REPLACE_EXISTING );
			} catch ( final IOException e )
			{
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * Get all sub paths
	 *
	 * @return Array containing the sub paths, will be empty if this isn't a directory
	 * @see #isFolder() to check if this is a directory or not
	 */
	public Collection<Path> getSubPaths()
	{
		return getSubPaths( true );
	}

	/**
	 * Get all sub paths
	 *
	 * @param includeFolders Whether or not (sub-)folders should be included
	 * @return Array containing the sub paths, will be empty if this isn't a directory
	 * @see #isFolder() to check if this is a directory or not
	 */
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

	@Override public boolean equals(final Object o)
	{
		return this == o || ( o != null && getClass() == o.getClass() && path.equals( ( ( Path ) o ).path ) );
	}

	@Override public int hashCode()
	{
		return path.hashCode();
	}

	/**
	 * Get a collection containing all the sub paths that matches a given predicate
	 *
	 * @param filters Path filter
	 * @return Collection of all sub paths that match the filter
	 */
	@SafeVarargs public final Collection<Path> getSubPaths(final Predicate<Path>... filters)
	{
		Stream<Path> stream = getSubPaths().stream();
		for ( final Predicate<Path> filter : filters )
		{
			stream = stream.filter( filter );
		}
		return stream.collect( Collectors.toList() );
	}

	public void delete()
	{
		if ( Files.exists( getJavaPath() ) )
		{
			try
			{
				Files.delete( getJavaPath() );
			} catch ( IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	public FileReader getReader() throws FileNotFoundException
	{
		if ( !this.exists() )
		{
			return null;
		}
		return new FileReader( getJavaPath().toFile() );
	}

	public FileWriter getWriter() throws IOException
	{
		return new FileWriter( getJavaPath().toFile() );
	}
}
