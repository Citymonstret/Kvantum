package xyz.kvantum.server.api.account;

import lombok.NonNull;
import xyz.kvantum.server.api.AccountService;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.views.requesthandler.Middleware;
import xyz.kvantum.server.api.views.requesthandler.MiddlewareQueue;

public final class AuthenticationRequiredMiddleware extends Middleware {

    @Override public void handle(@NonNull final AbstractRequest request,
        @NonNull final MiddlewareQueue queue) {
        final IAccountManager accountManager = AccountService.getInstance().getGlobalAccountManager();
        if (accountManager != null && accountManager.getAccount(request.getSession()).isPresent()) {
            queue.handle(request);
        } else {
            request.internalRedirect(CoreConfig.Middleware.loginRedirect);
        }
    }

}
