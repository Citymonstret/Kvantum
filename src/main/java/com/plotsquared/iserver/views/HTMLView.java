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
import com.plotsquared.iserver.crush.syntax.ProviderFactory;
import com.plotsquared.iserver.crush.syntax.VariableProvider;
import com.plotsquared.iserver.util.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class HTMLView extends View implements CacheApplicable
{

    public HTMLView(String filter, Map<String, Object> options)
    {
        super( filter, "html", options );
        super.fileName = "{file}.html";
        super.defaultFile = "index";
    }

    @Override
    public boolean passes(Request request)
    {
        File file = getFile( request );
        request.addMeta( "html_file", file );
        return file.exists();
    }

    @Override
    public Response generate(final Request r)
    {
        File file = (File) r.getMeta( "html_file" );

        Response response = new Response( this );
        response.getHeader().set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML );
        response.setContent( FileUtils.getDocument( file, getBuffer() ) );
        return response;
    }

    @Override
    public HTMLProvider getFactory(final Request r)
    {
        return new HTMLProvider( r );
    }

    @Override
    public boolean isApplicable(Request r)
    {
        return true;
    }

    public class HTMLProvider implements ProviderFactory<HTMLProvider>, VariableProvider
    {

        private final Map<String, String> storage = new HashMap<>();

        public HTMLProvider(final Request r)
        {
            storage.put( "name", r.getMeta( "html_file" ) + ".html" );
        }

        @Override
        public HTMLProvider get(Request r)
        {
            return this;
        }

        @Override
        public String providerName()
        {
            return "document";
        }

        @Override
        public boolean contains(String variable)
        {
            return storage.containsKey( variable );
        }

        @Override
        public Object get(String variable)
        {
            return storage.get( variable );
        }
    }
}
