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
