package xyz.kvantum.server.api;

import lombok.Getter;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.util.Assert;

import java.util.HashMap;
import java.util.Map;

public final class AccountService {

    @Getter private static final AccountService instance = new AccountService();
    private static final String INTERNAL_CONTEXT = "__internal__";

    private final Map<String, IAccountManager> accountManagerMap = new HashMap<>();

    private AccountService() {
    }

    public IAccountManager getGlobalAccountManager() {
        return this.getAccountManager(INTERNAL_CONTEXT);
    }

    public IAccountManager getAccountManager(final String context) {
        return this.accountManagerMap.get(Assert.notNull(context));
    }

    public void setAccountManager(final String context, final IAccountManager accountManager) {
        this.accountManagerMap.put(Assert.notNull(context), Assert.notNull(accountManager));
    }

    public void setGlobalAccountManager(final IAccountManager manager) {
        this.setAccountManager(INTERNAL_CONTEXT, manager);
    }

}
