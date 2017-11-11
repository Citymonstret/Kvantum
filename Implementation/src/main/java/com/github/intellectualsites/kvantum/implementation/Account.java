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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.account.IAccount;
import com.github.intellectualsites.kvantum.api.account.IAccountManager;
import com.github.intellectualsites.kvantum.api.account.roles.AccountRole;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.orm.KvantumObjectFactory;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumConstructor;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumField;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumInsert;
import com.github.intellectualsites.kvantum.api.orm.annotations.KvantumObject;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.StringList;
import lombok.*;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@KvantumObject
@EqualsAndHashCode(of = { "username", "id" })
@NoArgsConstructor
@Entity("accounts")
@SuppressWarnings("WeakerAccess")
public class Account implements IAccount
{
    @Getter
    private static final KvantumObjectFactory<Account> kvantumAccountFactory =
            KvantumObjectFactory.from( Account.class );

    private static final String KEY_ROLE_LIST = "internalRoleList";

    @KvantumField( defaultValue = "-1" )
    @Id
    @Getter
    private int id;
    @KvantumField( defaultValue = "none" )
    @Getter
    @NonNull
    private String username;
    @KvantumField( defaultValue = "none" /* Just because */ )
    @NonNull
    private String password;
    @NonNull
    private Map<String, String> data;
    @Setter
    @Transient
    private transient IAccountManager manager;

    private StringList rawRoleList;
    private Collection<AccountRole> roleList;

    public Account(final int id, final String username, final String password, final Map<String, String> data)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.data = data;
    }

    @KvantumConstructor
    public Account(@KvantumInsert( "id" ) final int userID, @KvantumInsert( "username" ) final String username,
                   @KvantumInsert( "password" ) final String password)
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

    @Override
    public Collection<AccountRole> getAccountRoles()
    {
        if ( this.roleList == null )
        {
            this.roleList = new HashSet<>();
            this.rawRoleList = new StringList( getData( KEY_ROLE_LIST ).orElse( "" ) );
            for ( final String string : rawRoleList )
            {
                final Optional<AccountRole> roleOptional = manager.getAccountRole( string );
                if ( roleOptional.isPresent() )
                {
                    this.roleList.add( roleOptional.get() );
                } else
                {
                    Logger.warn("Account [%s] has account role [%s] stored," +
                            " but the role is not registered in  the account manager", getUsername(), string );
                }
            }
        }
        return this.roleList;
    }

    @Override
    public void addRole(final AccountRole role)
    {
        if ( this.roleList == null )
        {
            this.getAccountRoles();
        }
        if ( this.roleList.contains( role ) )
        {
            return;
        }
        this.roleList.add( role );
        this.rawRoleList.add( role.getRoleIdentifier() );
        this.setData( KEY_ROLE_LIST, rawRoleList.toString() );
    }

    @Override
    public void removeRole(final AccountRole role)
    {
        if ( this.roleList == null )
        {
            this.getAccountRoles();
        }
        if ( this.roleList.contains( role ) )
        {
            this.roleList.remove( role );
            this.rawRoleList.remove( role.getRoleIdentifier() );
            this.setData( KEY_ROLE_LIST, rawRoleList.toString() );
        }
    }

    @Override
    public boolean isPermitted(final String permissionKey)
    {
        if ( this.roleList == null )
        {
            this.getAccountRoles();
        }
        for ( final AccountRole role : this.getAccountRoles() )
        {
            if ( role.hasPermission( permissionKey ) )
            {
                return true;
            }
        }
        return false;
    }

}
