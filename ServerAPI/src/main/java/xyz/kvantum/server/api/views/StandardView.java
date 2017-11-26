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
package xyz.kvantum.server.api.views;

import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.cache.CacheApplicable;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.FileExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StandardView extends StaticFileView implements CacheApplicable
{

    private static final String CONSTANT_EXCLUDE_EXTENSIONS = "excludeExtensions";

    public StandardView(String filter, Map<String, Object> options)
    {
        super( filter, options, "STANDARD", new ArrayList<>( Arrays.asList( FileExtension.values() ) ) );

        if ( options.containsKey( CONSTANT_EXCLUDE_EXTENSIONS ) )
        {
            final List<FileExtension> toRemove = new ArrayList<>();
            final List list = (List) options.get( CONSTANT_EXCLUDE_EXTENSIONS );
            for ( Object o : list )
            {
                toRemove.addAll( super.extensionList.stream().filter( extension -> extension.matches( o.toString() )
                ).collect( Collectors.toList() ) );
            }
            super.extensionList.removeAll( toRemove );
        }
    }

    @Override
    public boolean isApplicable(AbstractRequest r)
    {
        final Optional<Boolean> cacheApplicableBoolean = getOptionSafe( "cacheApplicable" );
        return cacheApplicableBoolean.orElse( false );
    }

    @Override
    public void handle(final AbstractRequest r, final Response response)
    {
        super.handle( r, response ); // SUPER IMPORTANT!!!!!

        final FileExtension extension = r.getMetaUnsafe( "extension" );
        switch ( extension )
        {
            case PDF:
            case TXT:
            case ZIP:
            {
                final Path path = r.getMetaUnsafe( "file" );
                final String fileName = path.getEntityName();

                response.getHeader().set( Header.HEADER_CONTENT_DISPOSITION,
                        String.format( "attachment; filename=\"%s.%s\"", fileName, extension.getOption() ) );
                response.getHeader().set( Header.HEADER_CONTENT_TRANSFER_ENCODING, "binary" );
                response.getHeader().set( Header.HEADER_CONTENT_LENGTH, "" + r.<Long>getMetaUnsafe( "file_length" ) );
            }
            break;
            case LESS:
            {
                response.setContent( LessView.getLess( response.getContent() ) );
            }
            break;
            default:
                break;
        }
    }
}
