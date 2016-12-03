/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
