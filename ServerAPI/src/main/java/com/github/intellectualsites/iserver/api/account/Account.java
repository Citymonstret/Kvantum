/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.account;

import com.github.intellectualsites.iserver.api.util.Assert;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A data structure representing an account, managed by {@link IAccountManager}
 */
public class Account
{

    @Getter
    private final int id;
    @Getter
    private final String username;
    private final String password;
    private final String salt;
    private final Map<String, String> data;
    private final IAccountManager manager;

    /**
     * @param id       (Unique) account ID
     * @param username Account username
     * @param password Account password
     * @param salt     Salt used when generating password
     * @param manager  Account manager implementation
     */
    public Account(final int id, final String username, final String password, final String salt, final IAccountManager
            manager)
    {
        Assert.isPositive( id );
        Assert.notEmpty( username );
        Assert.notEmpty( password );
        Assert.notEmpty( salt );
        Assert.notNull( manager );

        this.username = username;
        this.password = password;
        this.salt = salt;
        this.id = id;
        this.data = new ConcurrentHashMap<>();
        this.manager = manager;

        this.manager.loadData( this );
    }

    public Map<String, String> getRawData()
    {
        return new HashMap<>( data );
    }

    public void internalMetaUpdate(final String key, final String value)
    {
        this.data.put( key, value );
    }

    public boolean passwordMatches(final String password)
    {
        return manager.checkPassword( password, this.password );
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

}
