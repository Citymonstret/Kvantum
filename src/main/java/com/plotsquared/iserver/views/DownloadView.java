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
import com.plotsquared.iserver.util.FileUtils;

import java.io.File;
import java.util.Map;

/**
 * Created 2015-05-01 for IntellectualServer
 *
 * @author Citymonstret
 */
public class DownloadView extends View
{

    public DownloadView(String filter, Map<String, Object> options)
    {
        super( filter, "download", options );
        super.relatedFolderPath = "/assets/downloads";
    }

    @Override
    public boolean passes(Request request)
    {
        Map<String, String> variables = request.getVariables();
        String file = variables.get( "file" ) + variables.get( "extension" );
        if ( file.endsWith( ".zip" ) )
        {
            request.addMeta( "file_type", "zip" );
        } else if ( file.endsWith( ".txt" ) )
        {
            request.addMeta( "file_type", "txt" );
        } else if ( file.endsWith( ".pdf" ) )
        {
            request.addMeta( "file_type", "pdf" );
        } else
        {
            return false;
        }
        request.addMeta( "zip_file", file );
        return ( new File( getFolder(), file ) ).exists();
    }


    @Override
    public Response generate(final Request r)
    {
        File file = new File( getFolder(), r.getMeta( "zip_file" ).toString() );
        byte[] bytes = FileUtils.getBytes( file, getBuffer() );
        Response response = new Response( this );
        response.getHeader().set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_OCTET_STREAM );
        response.getHeader().set( Header.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + r.getMeta( "zip_file" ).toString() + "\"" );
        response.getHeader().set( Header.HEADER_CONTENT_TRANSFER_ENCODING, "binary" );
        response.getHeader().set( Header.HEADER_CONTENT_LENGTH, "" + file.length() );
        response.setBytes( bytes );
        return response;
    }

}
