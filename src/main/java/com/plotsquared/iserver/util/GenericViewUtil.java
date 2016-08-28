/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
package com.plotsquared.iserver.util;

import com.plotsquared.iserver.object.FileType;
import com.plotsquared.iserver.object.Header;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.views.LessView;
import com.plotsquared.iserver.views.errors.View404;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public final class GenericViewUtil
{

    private static final Collection<String> image = new ArrayList<>();

    static
    {
        image.add( "png" );
        image.add( "ico" );
        image.add( "gif" );
        image.add( "jpg" );
        image.add( "jpeg" );
    }

    public static Response getGenericResponse(final File file, final Request request,
                                              final Response response, final String extension, final int buffer)
    {
        Assert.isValid( request );
        Assert.notNull( file, request, extension );

        final boolean isImage = CollectionUtil.containsIgnoreCase( image, extension );
        if ( isImage )
        {
            final byte[] imageBytes = FileUtils.getBytes( file, buffer );
            final String imageType = extension.equalsIgnoreCase( "ico" ) ?
                    "x-icon" : ( extension.equalsIgnoreCase( "jpg" ) ? "jpeg" : extension );
            response.getHeader().set( Header.HEADER_CONTENT_TYPE, "image/" + imageType + "; charset=utf-8" );
            response.setBytes( imageBytes );
        } else
        {
            final Optional<FileType> type = FileType.byExtension( extension );
            if ( !type.isPresent() )
            {
                return View404.construct( request.getResourceRequest().getResource() ).generate( request );
            }
            response.getHeader().set( Header.HEADER_CONTENT_TYPE, type.get().getContentType() );
            if ( type.get() != FileType.LESS )
            {
                response.setContent( FileUtils.getDocument( file, buffer ) );
            } else
            {
                response.setContent( LessView.getLess( file, buffer ) );
            }
        }
        return response;
    }

}
