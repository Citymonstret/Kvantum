/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver.api.views.errors;

import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Response;
import com.github.intellectualsites.iserver.api.util.FileUtils;
import com.github.intellectualsites.iserver.api.views.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Error extends View
{

    private static String template;

    static
    {
        File file;
        try
        {
            file = new File( ServerImplementation.getImplementation().getCoreFolder(), "templates" );
            if ( !file.exists() && !file.mkdir() )
            {
                Message.COULD_NOT_CREATE_FOLDER.log( file );
            }
            file = new File( file, "error.html" );
            if ( !file.exists() )
            {
                try
                {
                    file.createNewFile();
                } catch ( IOException e )
                {
                    e.printStackTrace();
                }
                File tempFile = new File( ViewException.class.getClassLoader().getResource( "template" + File.separator +
                        "error.html" ).toURI() );
                try ( FileInputStream in = new FileInputStream( tempFile ) )
                {
                    try ( FileOutputStream out = new FileOutputStream( file ) )
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

    private final int code;
    private final String desc;

    Error(final int code, final String desc)
    {
        super( "/", "error" );
        this.code = code;
        this.desc = desc;
    }

    @Override
    public boolean passes(final Request request)
    {
        return true;
    }

    @Override
    public Response generate(final Request r)
    {
        return new Response().setContent( template.replace( "{{code}}", code + "" ).replace( "{{message}}", desc )
                .replace( "{{path}}", r.getQuery().getFullRequest() ) );
    }

}
