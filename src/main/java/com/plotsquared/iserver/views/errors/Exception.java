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
package com.plotsquared.iserver.views.errors;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.logging.LogModes;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.FileUtils;
import com.plotsquared.iserver.views.View;

import java.io.*;
import java.net.URISyntaxException;

public class Exception extends View
{

    private static String template;

    static {
        File file;
        try
        {
            file = new File( Server.getInstance().getCoreFolder(), "templates" );
            if ( !file.exists() )
            {
                if ( !file.mkdir() )
                {
                    Server.getInstance().log( "Failed to create template folder :(", LogModes.MODE_ERROR );
                }
            }
            file = new File( file, "exception.html" );
            if ( !file.exists() )
            {
                try
                {
                    file.createNewFile();
                } catch ( IOException e )
                {
                    e.printStackTrace();
                }
                File tempFile = new File( Exception.class.getClassLoader().getResource( "template" + File.separator +
                        "exception.html" ).toURI() );
                try ( final FileInputStream in = new FileInputStream( tempFile ) )
                {
                    try ( final FileOutputStream out = new FileOutputStream( file ) )
                    {
                        FileUtils.copyFile( in, out, 1024 * 1024 * 16 );
                    } catch ( final java.lang.Exception e )
                    {
                        e.printStackTrace();
                    }
                } catch ( final java.lang.Exception e )
                {
                    e.printStackTrace();
                }
            }
            template = FileUtils.getDocument( file, 1024 * 1024 );
        } catch ( URISyntaxException e )
        {
            e.printStackTrace();
            template = "ERROR??";
        }
    }

    private final java.lang.Exception in;

    public Exception(final java.lang.Exception in)
    {
        super( "", "exception" );
        this.in = in;
    }

    @Override
    public Response generate(Request request)
    {
        StringWriter sw = new StringWriter();
        in.printStackTrace( new PrintWriter( sw ) );
        return new Response().setContent( template.replace( "{{path}}", request.getQuery().getResource() ).replace(
                        "{{exception}}", in.toString() ).replace( "{{cause}}", sw.toString().replace(System.getProperty
                        ("line.separator"), "<br/>\n") ).replace( "{{message}}", in.getMessage() ) );
    }
}
