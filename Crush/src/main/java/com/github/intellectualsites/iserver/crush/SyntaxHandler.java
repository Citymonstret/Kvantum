/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.crush;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.core.WorkerProcedure;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.util.IgnoreSyntax;
import com.github.intellectualsites.iserver.api.util.ProviderFactory;
import com.github.intellectualsites.iserver.api.views.RequestHandler;
import com.github.intellectualsites.iserver.crush.syntax.Syntax;

import java.util.HashMap;
import java.util.Map;

public class SyntaxHandler extends WorkerProcedure.StringHandler
{

    private final CrushEngine crushEngine;

    SyntaxHandler(final CrushEngine crushEngine)
    {
        this.crushEngine = crushEngine;
    }

    @Override
    public String act(final RequestHandler requestHandler, final Request request, final String in)
    {
        String out = in;

        if ( CoreConfig.debug )
        {
            ServerImplementation.getImplementation().log( "CrushEngine is reacting to %s", request );
        }
        if ( !( requestHandler instanceof IgnoreSyntax ) )
        {
            final Map<String, ProviderFactory> factories = new HashMap<>();
            for ( final ProviderFactory factory : crushEngine.providers )
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
        }
        return out;
    }
}
