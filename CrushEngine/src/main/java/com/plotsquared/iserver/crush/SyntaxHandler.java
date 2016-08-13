package com.plotsquared.iserver.crush;

import com.plotsquared.iserver.core.CoreConfig;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.core.WorkerProcedure;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.crush.syntax.IgnoreSyntax;
import com.plotsquared.iserver.crush.syntax.ProviderFactory;
import com.plotsquared.iserver.crush.syntax.Syntax;
import com.plotsquared.iserver.views.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public class SyntaxHandler extends WorkerProcedure.StringHandler
{

    private final Server server;
    private final CrushEngine crushEngine;

    SyntaxHandler(final Server server, final CrushEngine crushEngine)
    {
        this.server = server;
        this.crushEngine = crushEngine;
    }

    @Override
    public String act(RequestHandler requestHandler, Request request, String in)
    {
        if ( CoreConfig.debug )
        {
            Server.getInstance().log( "CrushEngine is reacting to %s", request );
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
