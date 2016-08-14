package com.plotsquared.iserver.files;

import java.io.File;

@SuppressWarnings( "unused" )
public class Path
{

    private final FileSystem fileSystem;
    private final String path;
    private final boolean isFolder;
    private final File file;

    private boolean exists;
    private Path[] subPaths;

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
        this.isFolder = isFolder;
        this.file = new File( fileSystem.coreFolder, path );
        this.exists = file.exists();
    }

    /**
     * Get the Java file representation of this path
     * @return Java file
     */
    final public File getFile()
    {
        return this.file;
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
        if ( isFolder )
        {
            return ( exists = getFile().mkdirs() );
        }
        try
        {
            exists = getFile().createNewFile();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return exists;
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

    /**
     * Get all sub paths
     * @return Array containing the sub paths, will be empty if this isn't a directory
     * @see #isFolder() to check if this is a directory or not
     */
    public Path[] getSubPaths() {
        if ( this.subPaths != null )
        {
            return this.subPaths;
        }
        if ( !this.exists )
        {
            return new Path[ 0 ];
        }
        if ( !this.isFolder )
        {
            this.subPaths = new Path[ 0 ];
            return this.subPaths;
        }
        final File[] files = file.listFiles();
        if ( files == null )
        {
            this.subPaths = new Path[ 0 ];
            return this.subPaths;
        }
        final Path[] paths = new Path[ files.length ];
        for ( int i = 0; i < files.length; i++ )
        {
            paths[ i ] = getPath( files[ i ].getName() );
        }
        return paths;
    }
}
