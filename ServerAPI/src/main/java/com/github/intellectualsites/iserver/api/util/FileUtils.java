/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.util;

import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.exceptions.IntellectualServerException;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@UtilityClass
public class FileUtils
{

    /**
     * Add files to a zip file
     *
     * @param zipFile Zip File
     * @param files   Files to add to the zip
     * @param delete  If the original files should be deleted
     * @throws Exception If anything goes wrong
     */
    public static void addToZip(final File zipFile, final File[] files, final boolean delete) throws Exception
    {
        Assert.notNull( zipFile, files );

        if ( !zipFile.exists() && !zipFile.createNewFile() )
        {
            throw new IntellectualServerException( "Couldn't create " + zipFile );
        }

        final File temporary = File.createTempFile( zipFile.getName(), "" );
        //noinspection ResultOfMethodCallIgnored
        temporary.delete();

        if ( !zipFile.renameTo( temporary ) )
        {
            throw new IntellectualServerException( "Couldn't rename " + zipFile + " to " + temporary );
        }

        final byte[] buffer = new byte[ 1024 * 16 ]; // 16mb

        ZipInputStream zis = new ZipInputStream( new FileInputStream( temporary ) );
        ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zipFile ) );

        ZipEntry e = zis.getNextEntry();
        while ( e != null )
        {
            String n = e.getName();

            boolean no = true;
            for ( File f : files )
            {
                if ( f.getName().equals( n ) )
                {
                    no = false;
                    break;
                }
            }

            if ( no )
            {
                zos.putNextEntry( new ZipEntry( n ) );
                int len;
                while ( ( len = zis.read( buffer ) ) > 0 )
                {
                    zos.write( buffer, 0, len );
                }
            }
            e = zis.getNextEntry();
        }
        zis.close();
        for ( File file : files )
        {
            InputStream in = new FileInputStream( file );
            zos.putNextEntry( new ZipEntry( file.getName() ) );

            int len;
            while ( ( len = in.read( buffer ) ) > 0 )
            {
                zos.write( buffer, 0, len );
            }

            zos.closeEntry();
            in.close();
        }
        zos.close();
        temporary.delete();

        if ( delete )
        {
            for ( File f : files )
            {
                f.delete();
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

    public static void copyFile(final File in, final File out)
    {
        try ( final FileInputStream inStream = new FileInputStream( in ) )
        {
            try ( final FileOutputStream outStream = new FileOutputStream( out ) )
            {
                copyFile( inStream, outStream, 16 * 1024 );
            } catch ( final Exception ee )
            {
                ee.printStackTrace();
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
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
                out.write( buffer, 0, length );
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

    public static byte[] getBytes(final File file, final int buffer)
    {
        byte[] bytes = new byte[ 0 ];
        try
        {
            BufferedInputStream stream = new BufferedInputStream( new FileInputStream( file ), buffer );
            bytes = IOUtils.toByteArray( stream );
            stream.close();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return bytes;
    }

    public static String getDocument(final File file, int buffer, boolean create)
    {
        final Optional<String> cacheEntry = ServerImplementation.getImplementation().getCacheManager().getCachedFile(
                file
                        .toString
                                () );
        if ( cacheEntry.isPresent() )
        {
            return cacheEntry.get();
        }

        StringBuilder document = new StringBuilder();
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

            BufferedReader reader = new BufferedReader( new FileReader( file ), buffer );
            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                document.append( line ).append( "\n" );
            }
            reader.close();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }

        final String content = document.toString();
        ServerImplementation.getImplementation().getCacheManager().setCachedFile( file.toString(), content );
        return content;
    }

    /**
     * Get the size of a file or directory
     *
     * @param file File
     * @return Size of file
     */
    public static long getSize(final File file)
    {
        long size = 0;
        if ( file.isDirectory() )
        {
            final File[] files = file.listFiles();
            if ( files == null )
                return size;
            for ( final File f : files )
                if ( f.isFile() )
                    size += f.length();
                else
                    size += getSize( file );
        } else if ( file.isFile() )
            size += file.length();
        return size;
    }
}
