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

/**
 * Created 2015-04-19 for Kvantum
 *
 * @author Citymonstret
 */
public class Error extends View
{

    private static String template = "not loaded";

    static
    {
        initTemplate();
    }

    private static void initTemplate()
    {
        final String resourcePath = "template/error.html";
        final Path folder = ServerImplementation.getImplementation().getFileSystem().getPath( "templates" );
        if ( !folder.exists() )
        {
            if ( !folder.create() )
            {
                Message.COULD_NOT_CREATE_FOLDER.log( folder );
                return;
            }
        }
        final Path path = folder.getPath( "error.html" );
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
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        template = path.readFile();
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
    public boolean passes(final AbstractRequest request)
    {
        return true;
    }

    @Override
    public Response generate(final AbstractRequest r)
    {
        return new Response().setContent( template.replace( "{{code}}", code + "" ).replace( "{{message}}", desc )
                .replace( "{{path}}", r.getQuery().getFullRequest() ) );
    }

}
