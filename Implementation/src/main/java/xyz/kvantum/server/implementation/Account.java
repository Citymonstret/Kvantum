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
package xyz.kvantum.server.implementation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotEmpty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.account.roles.AccountRole;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.orm.KvantumObjectFactory;
import xyz.kvantum.server.api.orm.annotations.KvantumConstructor;
import xyz.kvantum.server.api.orm.annotations.KvantumField;
import xyz.kvantum.server.api.orm.annotations.KvantumInsert;
import xyz.kvantum.server.api.orm.annotations.KvantumObject;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.StringList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@KvantumObject(checkValidity = true)
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

    @Min(-1)
    @KvantumField
    @Id
    @Getter
    private int id;
    @NotEmpty
    @KvantumField
    @Getter
    @NonNull
    private String username;
    @KvantumField
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
    public Account(@KvantumInsert(value = "id", defaultValue = "-1") final int userID,
                   @KvantumInsert(value = "username") final String username,
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
