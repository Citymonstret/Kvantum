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
package com.github.intellectualsites.kvantum.api.jtwig;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.template.TemplateHandler;
import com.github.intellectualsites.kvantum.api.template.TemplateSyntaxHandler;
import com.github.intellectualsites.kvantum.api.templates.TemplateManager;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;
import com.github.intellectualsites.kvantum.api.util.VariableProvider;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.DefaultEnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SyntaxHandler extends TemplateSyntaxHandler
{

    private final EnvironmentConfiguration configuration = new DefaultEnvironmentConfiguration();
    private final Map<String, JtwigTemplate> templateStorage = new HashMap<>();

    SyntaxHandler(TemplateHandler templateHandler)
    {
        super( templateHandler );
    }

    @Override
    protected String handle(final RequestHandler requestHandler, final AbstractRequest request, final String in)
    {
        String out = in;
        JtwigTemplate template = null;
        boolean shouldCache = false;
        if ( CoreConfig.Cache.enabled )
        {
            if ( templateStorage.containsKey( requestHandler.getName() ) )
            {
                template = templateStorage.get( requestHandler.getName() );
            } else
            {
                shouldCache = true;
            }
        }

        if ( template == null )
        {
            template = JtwigTemplate.inlineTemplate( out, configuration );
            if ( shouldCache )
            {
                templateStorage.put( requestHandler.getName(), template );
            }
        }

        final JtwigModel model = JtwigModel.newModel();
        final Map<String, ProviderFactory<? extends VariableProvider>> factories = new HashMap<>();

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
            model.with( entry.getKey(), entryObjects );
        }

        out = template.render( model );
        return out;
    }
}
