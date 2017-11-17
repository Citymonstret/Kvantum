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
package com.github.intellectualsites.kvantum.api.util;

import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.exceptions.KvantumException;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for common File operations (both NIO and IO types)
 */
@UtilityClass
public class FileUtils
{

    public static void copyResource(final String resourcePath, final Path path) throws Exception
    {
        if ( !Files.exists( path.getParent() ) )
        {
            Files.createDirectory( path.getParent() );
        }
        if ( !Files.exists( path ) )
        {
            Files.createFile( path );
        }
        try ( BufferedReader reader = new BufferedReader(
                new InputStreamReader( ClassLoader.getSystemResourceAsStream( resourcePath ) ) ) )
        {
            String line;
            try ( BufferedWriter writer = Files.newBufferedWriter( path ) )
            {
                while ( ( line = reader.readLine() ) != null )
                {
                    writer.write( line + "\r\n" );
                }
            }
        }
        Logger.info( "Successfully copied '%s' to '%s'", resourcePath, path.getFileName() );
    }

    /**
     * Add files to a zip file
     *
     * @param zipFile Zip File
     * @param files   Files to add to the zip
     * @throws Exception If anything goes wrong
     */
    public static void addToZip(final File zipFile, final File[] files) throws Exception
    {
        Assert.notNull( zipFile, files );

        final Map<String, String> env = new HashMap<String, String>()
        {
            {
                put( "create", "true" );
            }
        };
        final Path path = zipFile.toPath();
        final URI uri = URI.create( "jar:" + path.toUri() );
        try ( FileSystem fileSystem = FileSystems.newFileSystem( uri, env ) )
        {
            for ( final File file : files )
            {
                Files.move( file.toPath(), fileSystem.getPath( file.getName() ), StandardCopyOption.REPLACE_EXISTING );
            }
        }
    }

    public static File attemptFolderCreation(final File folder)
    {
        if ( !folder.exists() && !folder.mkdirs() )
        {
            Message.COULD_NOT_CREATE_FOLDER.log( folder );
        }
        return folder;
    }

    /**
     * Copy a file from one location to another
     *
     * @param in   Ingoing File
     * @param out  Outgoing File
     * @param size Byte Buffer Size (in bytes)
     */
    public static void copyFile(final InputStream in, final OutputStream out,
                                final int size)
    {
        Assert.notNull( in );
        Assert.notNull( out );
        try
        {
            final byte[] buffer = new byte[ size ];
            int length;
            while ( ( length = in.read( buffer ) ) > 0 )
            {
                out.write( buffer, 0, length );
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                in.close();
                out.close();
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get file contents as a string
     *
     * @param file   File to read
     * @param buffer File buffer
     * @return String
     */
    public static String getDocument(final File file, int buffer)
    {
        return getDocument( file, buffer, false );
    }

    public static String getDocument(final File file, final int buffer, final boolean create)
    {
        Optional<String> cacheEntry = Optional.empty();

        try
        {
            cacheEntry = ServerImplementation.getImplementation().getCacheManager().getCachedFile(
                    file.toString() );
        } catch ( final Throwable e )
        {
            new KvantumException( "Failed to read file (" + file + ") from cache", e ).printStackTrace();
        }

        if ( cacheEntry.isPresent() )
        {
            return cacheEntry.get();
        }

        final StringBuilder document = new StringBuilder();
        try
        {
            if ( !file.exists() )
            {
                if ( !file.getParentFile().exists() )
                {
                    file.getParentFile().mkdirs();
                }
                if ( create )
                {
                    file.createNewFile();
                    return "";
                }
            }

            try ( BufferedReader reader = new BufferedReader( new FileReader( file ), buffer ) )
            {
                String line;
                while ( ( line = reader.readLine() ) != null )
                {
                    document.append( line ).append( "\n" );
                }
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }

        final String content = document.toString();
        ServerImplementation.getImplementation().getCacheManager().setCachedFile( file.toString(), content );
        return content;
    }

}
