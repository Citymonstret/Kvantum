/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.account.IAccount;
import com.github.intellectualsites.iserver.api.account.IAccountManager;
import com.github.intellectualsites.iserver.api.util.Assert;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A data structure representing an account, managed by {@link IAccountManager}
 */
@NoArgsConstructor
@Entity("accounts")
public class Account implements IAccount
{

    @Id
    @Getter
    private int id;
    @Getter
    @NonNull
    private String username;
    @Getter
    @NonNull
    private String password;
    @NonNull
    private Map<String, String> data;
    @Setter
    @Transient
    private transient IAccountManager manager;

    public Account(final int id, final String username, final String password, final Map<String, String> data)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.data = data;
    }

    public Account(final int userID, final String username, final String password)
    {
        this( userID, username, password, getDefaultDataSet() );
    }

    private static Map<String, String> getDefaultDataSet()
    {
        final Map<String, String> map = new ConcurrentHashMap<>();
        map.put( "created", "true" );
        return map;
    }

    @Override
    public Map<String, String> getRawData()
    {
        return new HashMap<>( data );
    }

    @Override
    public void internalMetaUpdate(final String key, final String value)
    {
        this.data.put( key, value );
    }

    @Override
    public boolean passwordMatches(final String password)
    {
        return IAccountManager.checkPassword( password, this.password );
    }

    @Override
    public Optional<String> getData(final String key)
    {
        Assert.notEmpty( key );

        return Optional.ofNullable( data.get( key ) );
    }

    @Override
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

    @Override
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
