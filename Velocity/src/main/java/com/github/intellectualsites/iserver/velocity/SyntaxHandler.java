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
package com.github.intellectualsites.iserver.velocity;

import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.template.TemplateHandler;
import com.github.intellectualsites.iserver.api.template.TemplateSyntaxHandler;
import com.github.intellectualsites.iserver.api.templates.TemplateManager;
import com.github.intellectualsites.iserver.api.util.ProviderFactory;
import com.github.intellectualsites.iserver.api.util.VariableProvider;
import com.github.intellectualsites.iserver.api.views.RequestHandler;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SyntaxHandler extends TemplateSyntaxHandler
{

    SyntaxHandler(TemplateHandler templateHandler)
    {
        super( templateHandler );
    }

    @Override
    protected String handle(RequestHandler requestHandler, Request request, String in)
    {
        String out = in;
        final Map<String, ProviderFactory<? extends VariableProvider>> factories = new HashMap<>();
        final Map<String, Object> objects = new HashMap<>();

        for ( final ProviderFactory<? extends VariableProvider> factory : TemplateManager.get().getProviders() )
        {
            factories.put( factory.providerName().toLowerCase(), factory );
        }
        final ProviderFactory z = requestHandler.getFactory( request );
        if ( z != null )
        {
            factories.put( z.providerName().toLowerCase(), z );
        }
        factories.put( "request", request );

        for ( final Map.Entry<String, ProviderFactory<? extends VariableProvider>> entry : factories.entrySet() )
        {
            final Map<String, Object> entryObjects = new HashMap<>();
            final Optional<? extends VariableProvider> providerOptional = entry.getValue().get( request );
            if ( !providerOptional.isPresent() )
            {
                continue;
            }
            final VariableProvider provider = providerOptional.get();
            entryObjects.putAll( provider.getAll() );
            objects.put( entry.getKey(), entryObjects );
        }

        final VelocityContext context = new VelocityContext( objects );
        final StringWriter writer = new StringWriter();
        Velocity.evaluate( context, writer, "SyntaxHandler", out );
        out = writer.toString();
        return out;
    }

}
