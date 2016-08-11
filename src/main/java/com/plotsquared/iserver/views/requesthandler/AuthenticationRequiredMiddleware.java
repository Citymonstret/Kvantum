package com.plotsquared.iserver.views.requesthandler;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.Request;

public class AuthenticationRequiredMiddleware extends Middleware {

    @Override
    public void handle(Request request, MiddlewareQueue queue) {
        if (Server.getInstance().getAccountManager().getAccount(request.getSession()) == null) {
            request.internalRedirect("login");
        } else {
            queue.handle(request);
        }
    }

}
