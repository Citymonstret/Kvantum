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
package com.plotsquared.iserver.crush;

import com.plotsquared.iserver.core.CoreConfig;
import com.plotsquared.iserver.core.IntellectualServer;
import com.plotsquared.iserver.core.WorkerProcedure;
import com.plotsquared.iserver.crush.syntax.IgnoreSyntax;
import com.plotsquared.iserver.crush.syntax.ProviderFactory;
import com.plotsquared.iserver.crush.syntax.Syntax;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.views.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public class SyntaxHandler extends WorkerProcedure.StringHandler
{

    private final IntellectualServer server;
    private final CrushEngine crushEngine;

    SyntaxHandler(final IntellectualServer server, final CrushEngine crushEngine)
    {
        this.server = server;
        this.crushEngine = crushEngine;
    }

    @Override
    public String act(RequestHandler requestHandler, Request request, String in)
    {
        if ( CoreConfig.debug )
        {
            com.plotsquared.iserver.core.ServerImplementation.getImplementation().log( "CrushEngine is reacting to %s", request );
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
                if ( syntax.matches( in ) )
                {
                    in = syntax.handle( in, request, factories );
                }
            }
        }
        return in;
    }
}
