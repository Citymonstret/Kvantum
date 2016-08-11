package com.intellectualsites.web.views.requesthandler;

import com.intellectualsites.web.object.Request;

public abstract class Middleware {

    public abstract void handle(final Request request, final MiddlewareQueue queue);

}
