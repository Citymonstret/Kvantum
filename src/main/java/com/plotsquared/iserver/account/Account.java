package com.plotsquared.iserver.account;

import com.plotsquared.iserver.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Account
{

    private final int id;
    private final String username;
    private final String password;
    private final String salt;
    private final Map<String, String> data;
    private final AccountManager manager;

    Account(int id, String username, String password, String salt, AccountManager manager)
    {
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.id = id;
        this.data = new ConcurrentHashMap<>();
        this.manager = manager;

        this.manager.loadData( this );
    }

    public String getUsername()
    {
        return username;
    }

    public Map<String, String> getRawData()
    {
        return new HashMap<>( data );
    }

    void internalMetaUpdate(final String key, final String value)
    {
        this.data.put( key, value );
    }

    public boolean passwordMatches(final String password)
    {
        return AccountManager.checkPassword( password, this.password );
    }

    public Optional<String> getData(final String key)
    {
        Assert.notEmpty( key );

        return Optional.ofNullable( data.get( key ) );
    }

    public void setData(final String key, final String value)
    {
        Assert.notEmpty( key );
        Assert.notEmpty( value );

        if ( data.containsKey( key ) )
        {
            removeData( key );
        }
        this.data.put( key, value );
        this.manager.setData( this, key, value );
    }

    public void removeData(final String key)
    {
        Assert.notEmpty( key );

        if ( !data.containsKey( key ) )
        {
            return;
        }
        this.data.remove( key );
        this.manager.removeData( this, key );
    }

    public int getId()
    {
        return id;
    }
}
