package com.plotsquared.iserver.matching;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.views.RequestHandler;

public abstract class Router
{

    public abstract RequestHandler match(final Request request);

    public abstract void add(final RequestHandler handler);

    public abstract void remove(final RequestHandler handler);

    public abstract void clear();

    public void dump(final Server server)
    {
    }

}
