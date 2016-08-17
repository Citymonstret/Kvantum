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

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.object.cache.CacheApplicable;
import com.plotsquared.iserver.util.GenericViewUtil;

import java.io.File;
import java.util.Map;

public class StandardView extends View implements CacheApplicable
{

    public StandardView(String filter, Map<String, Object> options)
    {
        super( filter, "STANDARD", options );
    }

    @Override
    public boolean passes(Request request)
    {
        String fileName, extension;
        final Map<String, String> variables = request.getVariables();

        fileName = variables.get( "file" );
        extension = variables.get( "extension" ).replace( ".", "" );

        if ( fileName.isEmpty() )
        {
            if ( containsOption( "defaultFile" ) )
            {
                fileName = getOption( "fileName" );
            } else
            {
                fileName = "index";
            }
        }

        if ( extension.isEmpty() )
        {
            if ( containsOption( "defaultExt" ) )
            {
                extension = getOption( "defaultExt" );
            } else
            {
                extension = "html";
            }
        }
        File file = new File( getFolder(), fileName + "." + extension );
        if ( !file.exists() )
        {
            return false;
        }
        request.addMeta( "stdfile", file );
        request.addMeta( "stdext", extension.toLowerCase() );
        return true;
    }

    @Override
    public boolean isApplicable(Request r)
    {
        return true;
    }

    @Override
    public Response generate(final Request r)
    {
        final File file = (File) r.getMeta( "stdfile" );
        final Response response = new Response( this );
        final String extension = r.getMeta( "stdext" ).toString();
        return GenericViewUtil.getGenericResponse( file, r, response, extension, getBuffer() );
    }
}
