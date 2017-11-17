/*
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
