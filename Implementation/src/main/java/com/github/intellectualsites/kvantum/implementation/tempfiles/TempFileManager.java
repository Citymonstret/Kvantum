/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
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
package com.github.intellectualsites.kvantum.implementation.tempfiles;

import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.util.AutoCloseable;
import com.github.intellectualsites.kvantum.api.util.ITempFileManager;

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
