/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
