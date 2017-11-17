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
