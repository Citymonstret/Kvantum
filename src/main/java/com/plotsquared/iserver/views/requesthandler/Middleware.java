package com.plotsquared.iserver.views.requesthandler;

import com.plotsquared.iserver.object.Request;

public abstract class Middleware
{

    public abstract void handle(final Request request, final MiddlewareQueue queue);

}
