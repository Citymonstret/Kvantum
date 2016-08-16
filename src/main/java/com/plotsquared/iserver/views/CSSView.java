//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
public class CSSView extends View implements CacheApplicable
{

    public CSSView(String filter, Map<String, Object> options)
    {
        super( filter, "css", options );
        super.relatedFolderPath = "/assets/css";
        super.fileName = "{file}.css";
    }

    @Override
    public boolean passes(Request request)
    {
        File file = getFile( request );
        request.addMeta( "css_file", file );
        return file.exists();
    }

    @Override
    public Response generate(final Request r)
    {
        File file = (File) r.getMeta( "css_file" );
        Response response = new Response( this );
        response.getHeader().set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_CSS );
        response.setContent( FileUtils.getDocument( file, getBuffer() ) );
        return response;
    }

    @Override
    public boolean isApplicable(Request r)
    {
        return true;
    }
}
