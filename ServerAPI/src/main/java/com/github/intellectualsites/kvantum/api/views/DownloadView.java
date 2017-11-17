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
package com.github.intellectualsites.kvantum.api.views;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.util.FileExtension;
import com.github.intellectualsites.kvantum.api.util.IgnoreSyntax;
import com.github.intellectualsites.kvantum.files.Path;

import java.util.Map;

/**
 * Created 2015-05-01 for Kvantum
 *
 * @author Citymonstret
 */
public class DownloadView extends StaticFileView implements IgnoreSyntax
{

    public DownloadView(String filter, Map<String, Object> options)
    {
        super( filter, options, "download", FileExtension.DOWNLOADABLE );
        super.relatedFolderPath = "/assets/downloads";
    }

    @Override
    public void handle(final AbstractRequest r, final Response response)
    {
        final Path path = r.getMetaUnsafe( "path" );
        final String fileName = path.getEntityName();
        final FileExtension extension = r.getMetaUnsafe( "extension" );

        response.getHeader().set( Header.HEADER_CONTENT_DISPOSITION,
                String.format( "attachment; filename=\"%s.%s\"", fileName, extension.getOption() ) );
        response.getHeader().set( Header.HEADER_CONTENT_TRANSFER_ENCODING, "binary" );
        response.getHeader().set( Header.HEADER_CONTENT_LENGTH, "" + r.<Long>getMetaUnsafe( "file_length" ) );
    }

}
