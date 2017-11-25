/*
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
package xyz.kvantum.server.implementation.tempfiles;

import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.AutoCloseable;
import xyz.kvantum.server.api.util.ITempFileManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

final public class TempFileManager extends AutoCloseable implements ITempFileManager
{

    private final Set<WeakReference<Path>> pathReferences = new HashSet<>();

    @Override
    public Optional<Path> createTempFile()
    {
        try
        {
            final Path path = Files.createTempFile( null, null );
            path.toFile().deleteOnExit();
            this.pathReferences.add( new WeakReference<>( path ) );
            return Optional.of( path );
        } catch ( final IOException e )
        {
            Logger.error( "Failed to create temp file: %s", e.getMessage() );
        }
        return Optional.empty();
    }

    @Override
    public void clearTempFiles()
    {
        for ( final WeakReference<Path> weakReference : this.pathReferences )
        {
            final Path path;
            if ( ( path = weakReference.get() ) != null )
            {
                try
                {
                    Files.delete( path );
                } catch ( final IOException e )
                {
                    Logger.error( "Failed to delete temp file [%s]: %s", path.getFileName(), e.getMessage() );
                }
            }
        }
    }

    @Override
    protected void handleClose()
    {
        this.clearTempFiles();
    }
}
