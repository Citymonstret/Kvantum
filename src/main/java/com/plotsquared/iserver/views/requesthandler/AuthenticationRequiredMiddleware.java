package com.plotsquared.iserver.views.requesthandler;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.extra.accounts.AccountManager;
import com.plotsquared.iserver.object.Request;

import java.util.Optional;

public class AuthenticationRequiredMiddleware extends Middleware
{

    @Override
    public void handle(Request request, MiddlewareQueue queue)
    {
        final Optional<AccountManager> accountManager = Server.getInstance().getAccountManager();
        if ( accountManager.isPresent() && accountManager.get().getAccount( request.getSession() ) != null )
        {
            queue.handle( request );
        } else
        {
            request.internalRedirect( "login" );
        }
    }

}
