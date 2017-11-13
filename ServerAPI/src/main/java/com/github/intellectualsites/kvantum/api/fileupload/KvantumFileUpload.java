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
package com.github.intellectualsites.kvantum.api.fileupload;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileCleaningTracker;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for {@link FileUpload Commons FileUpload}
 */
@SuppressWarnings("ALL")
public class KvantumFileUpload extends FileUpload
{

    /**
     * Initialize a new file upload handler that stores the uploaded
     * file in a temporary file, with the default {@link DiskFileItemFactory#DEFAULT_SIZE_THRESHOLD}
     * size threshold
     */
    public KvantumFileUpload()
    {
        this( DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD );
    }

    /**
     * Initialize a new file upload handler that stores the uploaded
     * file in a temporary file
     *
     * @param threshold Size threshold
     */
    public KvantumFileUpload(final int threshold)
    {
        this( getDefaultFileItemFactory( threshold ) );
    }

    /**
     * Initialize a new file upload handler
     *
     * @param fileItemFactory File item factory
     */
    public KvantumFileUpload(final FileItemFactory fileItemFactory)
    {
        super( fileItemFactory );
    }

    private static FileItemFactory getDefaultFileItemFactory(final int threshold)
    {
        final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        diskFileItemFactory.setFileCleaningTracker( new FileCleaningTracker() );
        diskFileItemFactory.setSizeThreshold( threshold );
        return diskFileItemFactory;
    }

    /**
     * Delegate for {@link FileUpload#getItemIterator(RequestContext)}
     */
    public FileItemIterator getItemIterator(final KvantumFileUploadContext request) throws FileUploadException, IOException
    {
        return super.getItemIterator( request );
    }

    /**
     * Delegate for {@link FileUpload#parseRequest(RequestContext)}
     */
    public List<FileItem> parseRequest(final KvantumFileUploadContext request) throws FileUploadException
    {
        return super.parseRequest( request );
    }

    /**
     * Delegate for {@link FileUpload#parseParameterMap(RequestContext)}
     */
    public Map<String, List<FileItem>> parseParameterMap(final KvantumFileUploadContext request) throws FileUploadException
    {
        return super.parseParameterMap( request );
    }

}
