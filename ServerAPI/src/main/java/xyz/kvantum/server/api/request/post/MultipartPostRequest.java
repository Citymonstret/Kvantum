/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.server.api.request.post;

import lombok.Getter;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.fileupload.KvantumFileUploadContext;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MultipartPostRequest extends PostRequest
{

    @Getter
    private KvantumFileUploadContext.KvantumFileUploadContextParsingResult parsingResult;

    public MultipartPostRequest(final AbstractRequest parent, final String rawRequest)
    {
        super( parent, rawRequest, true );
    }

    @Override
    protected void parseRequest(final String rawRequest)
    {
        this.parsingResult = KvantumFileUploadContext.from( this.getParent() );
        if ( parsingResult.getStatus() == KvantumFileUploadContext.KvantumFileUploadContextParsingStatus.SUCCESS )
        {
            final KvantumFileUploadContext context = this.parsingResult.getContext();
            try
            {
                final FileItemIterator itemIterator = ServerImplementation.getImplementation().getGlobalFileUpload()
                        .getItemIterator( context );
                FileItemStream item;
                while ( itemIterator.hasNext() )
                {
                    item = itemIterator.next();
                    if ( !item.isFormField() )
                    {
                        continue; // We do not handle files, that is up to the application implementations
                    }
                    try ( final InputStream inputStream = item.openStream() )
                    {
                        final List lines = IOUtils.readLines( inputStream );
                        if ( lines.size() != 1 )
                        {
                            Logger.warn( "FileItem simple field line count is not 0 (Request: {})", getParent() );
                            continue;
                        }
                        this.getVariables().put( item.getFieldName(), lines.get( 0 ).toString() );
                    }
                }
            } catch ( final FileUploadException | IOException e )
            {
                e.printStackTrace();
            }
        } else
        {
            Logger.warn( "Failed to parse multipart request: {}", parsingResult.getStatus() );
        }
    }

    @Override
    public EntityType getEntityType()
    {
        return EntityType.FORM_MULTIPART;
    }
}
