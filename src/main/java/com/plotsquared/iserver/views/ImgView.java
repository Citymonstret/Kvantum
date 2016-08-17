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
package com.plotsquared.iserver.views;

import com.plotsquared.iserver.object.Header;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.object.cache.CacheApplicable;
import com.plotsquared.iserver.util.FileUtils;

import java.io.File;
import java.util.Map;

/**
 * Created 2015-04-21 for IntellectualServer
 *
 * @author Citymonstret
 */
public class ImgView extends View implements CacheApplicable
{

    public ImgView(String filter, Map<String, Object> options)
    {
        super( filter, "img", options );
        super.relatedFolderPath = "/assets/img";
    }

    @Override
    public boolean passes(Request request)
    {
        Map<String, String> variables = request.getVariables();
        String file = variables.get( "file" ) + variables.get( "extension" );

        if ( file.endsWith( ".png" ) )
        {
            request.addMeta( "img_type", "png" );
        } else if ( file.endsWith( ".ico" ) )
        {
            request.addMeta( "img_type", "x-icon" );
        } else if ( file.endsWith( ".gif" ) )
        {
            request.addMeta( "img_type", "gif" );
        } else if ( file.endsWith( ".jpg" ) || file.endsWith( ".jpeg" ) )
        {
            request.addMeta( "img_type", "jpeg" );
        } else
        {
            return false;
        }
        request.addMeta( "img_file", file );
        return ( new File( getFolder(), file ) ).exists();
    }

    @Override
    public Response generate(final Request r)
    {
        File file = new File( getFolder(), r.getMeta( "img_file" ).toString() );
        byte[] bytes = FileUtils.getBytes( file, getBuffer() );
        Response response = new Response( this );
        response.getHeader().set( Header.HEADER_CONTENT_TYPE, "image/" + r.getMeta( "img_type" ) + "; charset=utf-8" );
        response.setBytes( bytes );
        return response;
    }

    @Override
    public boolean isApplicable(Request r)
    {
        return false;
    }
}
