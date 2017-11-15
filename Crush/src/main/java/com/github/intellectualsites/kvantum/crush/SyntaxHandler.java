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
package com.github.intellectualsites.kvantum.crush;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.template.TemplateSyntaxHandler;
import com.github.intellectualsites.kvantum.api.templates.TemplateManager;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import com.github.intellectualsites.kvantum.crush.syntax.Syntax;

import java.util.HashMap;
import java.util.Map;

public class SyntaxHandler extends TemplateSyntaxHandler
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
