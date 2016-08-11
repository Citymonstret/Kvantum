package com.plotsquared.iserver.views.requesthandler;


import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.util.Assert;

import java.util.ArrayDeque;
import java.util.Queue;

public final class MiddlewareQueue
{

    private final Queue<Middleware> queue = new ArrayDeque<>();

    private boolean finished = false;

    public void add(final Middleware middleware)
    {
        this.queue.add( middleware );
    }

    public void handle(final Request request)
    {
        Assert.isValid( request );

        final Middleware next = this.queue.poll();
        if ( next != null )
        {
            next.handle( request, this );
        } else
        {
            finished = true;
        }
    }

    public final boolean finished()
    {
        return finished;
    }

}
