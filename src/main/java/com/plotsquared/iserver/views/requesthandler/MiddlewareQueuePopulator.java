package com.plotsquared.iserver.views.requesthandler;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.logging.LogModes;

import java.util.ArrayList;
import java.util.Collection;

public final class MiddlewareQueuePopulator
{

    private final Collection<Class<? extends Middleware>> middlewares = new ArrayList<>();

    public void add(final Class<? extends Middleware> middleware)
    {
        if ( Middleware.class == middleware )
        {
            return;
        }
        try
        {
            middleware.getConstructor();
        } catch ( final Exception e )
        {
            Server.getInstance().log( "Middleware '" + middleware + "' doesn't have a default constructor, skipping it!",
                    LogModes.MODE_WARNING );
            return;
        }
        this.middlewares.add( middleware );
    }

    public MiddlewareQueue generateQueue()
    {
        final MiddlewareQueue queue = new MiddlewareQueue();
        middlewares.forEach( clazz ->
        {
            try
            {
                queue.add( clazz.newInstance() );
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        } );
        return queue;
    }

}
