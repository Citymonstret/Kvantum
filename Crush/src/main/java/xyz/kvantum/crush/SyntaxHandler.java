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
package xyz.kvantum.crush;

import xyz.kvantum.crush.syntax.Syntax;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.template.TemplateSyntaxHandler;
import xyz.kvantum.server.api.templates.TemplateManager;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.views.RequestHandler;

import java.util.HashMap;
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
        final Map<String, ProviderFactory> factories = new HashMap<>();
        for ( final ProviderFactory factory : TemplateManager.get().getProviders() )
        {
            factories.put( factory.providerName().toLowerCase(), factory );
        }
        final ProviderFactory z = requestHandler.getFactory( request );
        if ( z != null )
        {
            factories.put( z.providerName().toLowerCase(), z );
        }
        factories.put( "request", request );
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
