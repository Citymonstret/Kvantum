package com.plotsquared.iserver;

import com.plotsquared.iserver.files.FileSystem;

import java.nio.file.Path;

final class IntellectualFileSystem extends FileSystem
{

    IntellectualFileSystem(final Path coreFolder)
    {
        super( coreFolder, new FileCacheImplementation() );
    }

}
