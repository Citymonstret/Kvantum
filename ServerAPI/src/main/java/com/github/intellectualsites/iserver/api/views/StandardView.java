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
package com.github.intellectualsites.iserver.api.views;

import com.github.intellectualsites.iserver.api.cache.CacheApplicable;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Header;
import com.github.intellectualsites.iserver.api.response.Response;
import com.github.intellectualsites.iserver.api.util.FileExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StandardView extends StaticFileView implements CacheApplicable
{

    private static final String CONSTANT_EXCLUDE_EXTENSIONS = "excludeExtensions";

    public StandardView(String filter, Map<String, Object> options)
    {
        super( filter, options, "STANDARD", new ArrayList<>( Arrays.asList( FileExtension.values() ) ) );
        super.fileName = "{file}.{extension}";

        if ( !options.containsKey( "defaultExtension" ) )
        {
            super.setOption( "defaultExtension", "html" );
        }

        super.defaultFile = ( getOptionSafe( "defaultFile" ).orElse( "index" ) ).toString();

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
    public boolean isApplicable(Request r)
    {
        return false;
    }

    @Override
    public Response generate(final Request r)
    {
        final Response response = super.generate( r );

        final FileExtension extension = (FileExtension) r.getMeta( "extension" );
        switch ( extension )
        {
            case PDF:
            case TXT:
            case ZIP:
            {
                response.getHeader().set( Header.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + extension.getOption
                        () + "\"" );
                response.getHeader().set( Header.HEADER_CONTENT_TRANSFER_ENCODING, "binary" );
                response.getHeader().set( Header.HEADER_CONTENT_LENGTH, r.getMeta( "file_length" ).toString() );
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

        return response;
    }
}
