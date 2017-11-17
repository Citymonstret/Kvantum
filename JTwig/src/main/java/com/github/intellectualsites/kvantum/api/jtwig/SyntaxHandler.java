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
