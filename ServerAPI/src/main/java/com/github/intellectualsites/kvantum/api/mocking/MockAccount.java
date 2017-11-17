/*
 *
 *    Copyright (C) 2017 IntellectualSites
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
package com.github.intellectualsites.kvantum.api.mocking;

import com.github.intellectualsites.kvantum.api.account.IAccount;
import com.github.intellectualsites.kvantum.api.account.IAccountManager;
import com.github.intellectualsites.kvantum.api.account.roles.AccountRole;
import lombok.Data;

import java.util.*;

@Data
public class MockAccount implements IAccount
{

    private Map<String, String> rawData = new HashMap<>();
    private int id = (int) ( Math.random() * 10_000 );
    private String username = UUID.randomUUID().toString();
    private IAccountManager manager;
    private Collection<AccountRole> accountRoles = new HashSet<>();

    @Override
    public void internalMetaUpdate(final String key, final String value)
    {
        this.rawData.put( "meta." + key, value );
    }

    @Override
    public boolean passwordMatches(final String password)
    {
        return true;
    }

    @Override
    public Optional<String> getData(final String key)
    {
        return Optional.ofNullable( rawData.getOrDefault( key, null ) );
    }

    @Override
    public void setData(final String key, final String value)
    {
        this.rawData.put( key, value );
    }

    @Override
    public void removeData(final String key)
    {
        this.rawData.remove( key );
    }

    @Override
    public void addRole(final AccountRole role)
    {
        this.accountRoles.add( role );
    }

    @Override
    public void removeRole(final AccountRole role)
    {
        this.accountRoles.remove( role );
    }

    @Override
    public boolean isPermitted(final String permissionKey)
    {
        return true;
    }
}
