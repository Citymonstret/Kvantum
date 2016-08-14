package com.plotsquared.iserver.files;

import java.io.File;

public class Path
{

    private final FileSystem fileSystem;
    private final String path;
    private final boolean isFolder;
    private final File file;

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
    }

    final public File getFile()
    {
        return this.file;
    }

    @Override
    final public String toString()
    {
        return this.path;
    }

    public boolean isFolder()
    {
        return isFolder;
    }

    public Path getPath(final String path)
    {
        return fileSystem.getPath( this, path );
    }

    public String getExtension()
    {
        if ( this.isFolder )
        {
            return "";
        }
        final String[] parts = this.path.split( "\\." );
        return parts[ parts.length - 1 ];
    }

    public Path[] getSubPaths() {
        if ( this.subPaths != null )
        {
            return this.subPaths;
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
