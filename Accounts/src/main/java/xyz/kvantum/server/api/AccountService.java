/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
