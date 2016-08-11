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

        final boolean isImage = image.contains( extension );
        if ( isImage )
        {
            byte[] imageBytes = FileUtils.getBytes( file, buffer );
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
