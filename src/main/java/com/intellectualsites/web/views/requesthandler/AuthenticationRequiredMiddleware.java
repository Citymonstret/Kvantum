package com.intellectualsites.web.views.requesthandler;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.Request;

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
