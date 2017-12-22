/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.crush;

import xyz.kvantum.crush.syntax.Syntax;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.template.TemplateSyntaxHandler;
import xyz.kvantum.server.api.templates.TemplateManager;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;
import xyz.kvantum.server.api.views.RequestHandler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SyntaxHandler extends TemplateSyntaxHandler
{

    private final CrushEngine crushEngine;

    SyntaxHandler(final CrushEngine crushEngine)
    {
        super( crushEngine );
        this.crushEngine = crushEngine;
    }

    @Override
    public String handle(final RequestHandler requestHandler, final AbstractRequest request, final String in)
    {
        String out = in;
        final Map<String, ProviderFactory<? extends VariableProvider>> factories = new HashMap<>();
        for ( final ProviderFactory<? extends VariableProvider> factory : TemplateManager.get().getProviders() )
        {
            factories.put( factory.providerName().toLowerCase( Locale.ENGLISH ), factory );
        }
        final ProviderFactory<? extends VariableProvider> z = requestHandler.getFactory( request );
        if ( z != null )
        {
            factories.put( z.providerName().toLowerCase( Locale.ENGLISH ), z );
        }
        factories.put( "request", request );
        factories.putAll( request.getModels() );
        // This is how the crush engine works.
        // Quite simple, yet powerful!
        for ( final Syntax syntax : crushEngine.syntaxCollection )
        {
            if ( syntax.matches( out ) )
            {
                out = syntax.handle( out, request, factories );
            }
        }

        return out;
    }
}
