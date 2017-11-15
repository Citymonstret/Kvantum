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
package com.github.intellectualsites.kvantum.api.views;

import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.matching.FilePattern;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.util.FileExtension;
import com.github.intellectualsites.kvantum.files.Path;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public abstract class StaticFileView extends View
{

    protected final Collection<FileExtension> extensionList;

    public StaticFileView(String filter, Map<String, Object> options, String name, Collection<FileExtension> extensions)
    {
        super( filter, name, options );
        this.extensionList = extensions;
    }

    @Override
    final public boolean passes(final AbstractRequest request)
    {
        final Map<String, String> variables = request.getVariables();
        FileExtension fileExtension;

        if ( !variables.containsKey( "extension" ) )
        {
            final Optional<String> extensionOptional = getOptionSafe( "extension" );
            extensionOptional.ifPresent( s -> variables.put( "extension", s ) );
        }

        check:
        {
            for ( final FileExtension extension : extensionList )
            {
                if ( extension.matches( variables.get( "extension" ) ) )
                {
                    fileExtension = extension;
                    break check;
                }
            }
            Logger.error( "Unknown file extension: " + variables.get( "extension" ) );
            return false; // None matched
        }

        final FilePattern.FileMatcher fileMatcher = getFilePattern().matcher( () -> variables );
        request.addMeta( "fileMatcher", fileMatcher );
        request.addMeta( "extension", fileExtension );

        final Path file = getFile( request );
        request.addMeta( "file", file );

        final boolean exists = file.exists();
        if ( exists )
        {
            request.addMeta( "file_length", file.length() );
        }
        return exists;
    }

    @Override
    public void handle(final AbstractRequest r, final Response response)
    {
        final Path path = (Path) r.getMeta( "file" );
        final FileExtension extension = (FileExtension) r.getMeta( "extension" );
        response.getHeader().set( Header.HEADER_CONTENT_TYPE, extension.getContentType() );
        if ( extension.getReadType() == FileExtension.ReadType.BYTES )
        {
            response.setBytes( path.readBytes() );
        } else
        {
            response.setContent( extension.getComment( "Served to you by Kvantum" ) + System.lineSeparator()
                    + path.readFile() );
        }
    }
}
