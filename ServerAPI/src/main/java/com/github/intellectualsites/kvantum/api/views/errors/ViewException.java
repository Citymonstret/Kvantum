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
package com.github.intellectualsites.kvantum.api.views.errors;

import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.util.FileUtils;
import com.github.intellectualsites.kvantum.api.views.View;
import com.github.intellectualsites.kvantum.files.Path;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ViewException extends View
{

    private static String template = "not loaded";

    static
    {
        initTemplate();
    }

    private static void initTemplate()
    {
        final String resourcePath = "template/exception.html";
        final Path folder = ServerImplementation.getImplementation().getFileSystem().getPath( "templates" );
        if ( !folder.exists() )
        {
            if ( !folder.create() )
            {
                Message.COULD_NOT_CREATE_FOLDER.log( folder );
                return;
            }
        }
        final Path path = folder.getPath( "exception.html" );
        if ( !path.exists() )
        {
            if ( !path.create() )
            {
                Logger.error( "could not create file: '%s'", path );
                return;
            }
            try
            {
                FileUtils.copyResource( resourcePath, path.getJavaPath() );
                template = path.readFile();
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    private final java.lang.Exception in;

    public ViewException(final java.lang.Exception in)
    {
        super( "", "exception" );
        this.in = in;
    }

    @Override
    public Response generate(AbstractRequest request)
    {
        StringWriter sw = new StringWriter();
        in.printStackTrace( new PrintWriter( sw ) );
        return new Response().setContent( template.replace( "{{path}}", request.getQuery().getResource() ).replace(
                "{{exception}}", in.toString() ).replace( "{{cause}}", sw.toString().replace( System.getProperty
                ( "line.separator" ), "<br/>\n" ) ).replace( "{{message}}", in.getMessage() ) );
    }
}
