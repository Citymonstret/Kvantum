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
package com.github.intellectualsites.iserver.implementation.sqlite;

import com.github.intellectualsites.iserver.api.account.Account;
import com.github.intellectualsites.iserver.api.account.IAccountManager;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.api.util.SQLiteApplicationStructure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

@RequiredArgsConstructor
final public class SQLiteAccountManager implements IAccountManager
{

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Account> EMPTY_OPTIONAL = Optional.empty();

    @Getter
    private final SQLiteApplicationStructure applicationStructure;

    private static String getNewSalt()
    {
        return BCrypt.gensalt();
    }

    private static String hashPassword(final String password, final String salt)
    {
        return BCrypt.hashpw( password, salt );
    }

    @Override
    public void setup() throws Exception
    {
        this.applicationStructure.getDatabaseManager().executeUpdate( "CREATE TABLE IF NOT EXISTS account( id " +
                "INTEGER PRIMARY KEY, username VARCHAR(64), password VARCHAR(255), salt VARCHAR(255), CONSTRAINT " +
                "name_unique UNIQUE (username) )" );
        this.applicationStructure.getDatabaseManager().executeUpdate( "CREATE TABLE IF NOT EXISTS account_data ( id " +
                "INTEGER PRIMARY KEY, account_id INTEGER, `key` VARCHAR(255), `value` VARCHAR(255), UNIQUE" +
                "(account_id, `key`) )" );
        this.checkAdmin();
    }

    @Override
    public Optional<Account> createAccount(final String username, final String password)
    {
        Assert.notEmpty( username );
        Assert.notEmpty( password );

        Optional<Account> ret = EMPTY_OPTIONAL;
        if ( getAccount( username ).isPresent() )
        {
            return ret;
        }
        try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager()
                .prepareStatement( "INSERT INTO account(`username`, `password`, `salt`) VALUES(?, ?, ?)" ) )
        {
            statement.setString( 1, username );
            final String salt = getNewSalt();
            statement.setString( 2, hashPassword( password, salt ) );
            statement.setString( 3, salt );
            statement.executeUpdate();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        } finally
        {
            ret = getAccount( username );
        }
        return ret;
    }

    public Optional<Account> getAccount(final String username)
    {
        Assert.notEmpty( username );

        Optional<Integer> accountId = ServerImplementation.getImplementation().getCacheManager().getCachedId( username );
        Optional<Account> ret = EMPTY_OPTIONAL;
        if ( accountId.isPresent() )
        {
            ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount( accountId.get() );
        }
        if ( ret.isPresent() )
        {
            return ret;
        }
        try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager().prepareStatement(
                "SELECT * FROM `account` WHERE `username` = ?" ) )
        {
            statement.setString( 1, username );
            try ( final ResultSet resultSet = statement.executeQuery() )
            {
                if ( resultSet.next() )
                {
                    ret = Optional.of( getAccount( resultSet ) );
                }
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        ret.ifPresent( account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
        return ret;
    }

    public Optional<Account> getAccount(final int accountId)
    {
        Optional<Account> ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount( accountId );
        if ( ret.isPresent() )
        {
            return ret;
        }
        try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager().prepareStatement(
                "SELECT * FROM `account` WHERE `id` = ?" ) )
        {
            statement.setInt( 1, accountId );
            try ( final ResultSet resultSet = statement.executeQuery() )
            {
                if ( resultSet.next() )
                {
                    ret = Optional.of( getAccount( resultSet ) );
                }
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        ret.ifPresent( account -> ServerImplementation.getImplementation().getCacheManager().setCachedAccount( account ) );
        return ret;
    }

    private Account getAccount(final ResultSet resultSet) throws Exception
    {
        final int id = resultSet.getInt( "id" );
        final String username = resultSet.getString( "username" );
        final String password = resultSet.getString( "password" );
        final String salt = resultSet.getString( "salt" );
        return new Account( id, username, password, this );
    }

    @Override
    public void loadData(final Account account)
    {
        try ( final PreparedStatement statement = applicationStructure.getDatabaseManager().prepareStatement(
                "SELECT * FROM account_data WHERE account_id = ?" ) )
        {
            statement.setInt( 1, account.getId() );
            final ResultSet set = statement.executeQuery();
            while ( set.next() )
            {
                account.internalMetaUpdate( set.getString( "key" ), set.getString( "value" ) );
            }
            set.close();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void removeData(Account account, String key)
    {
        try ( final PreparedStatement statement = applicationStructure.getDatabaseManager().prepareStatement( "DELETE" +
                " FROM account_data WHERE account_id = ? AND `key` = ?" ) )
        {
            statement.setInt( 1, account.getId() );
            statement.setString( 2, key );
            statement.executeUpdate();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setData(Account account, String key, String value)
    {
        try ( final PreparedStatement statement = applicationStructure.getDatabaseManager().prepareStatement(
                "INSERT OR IGNORE INTO account_data(account_id, `key`, `value`) VALUES(?, ?, ?)" ) )
        {
            statement.setInt( 1, account.getId() );
            statement.setString( 2, key );
            statement.setString( 3, value );
            statement.executeUpdate();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        try ( final PreparedStatement statement = applicationStructure.getDatabaseManager().prepareStatement(
                "UPDATE account_data SET `value` = ? WHERE account_id = ? AND `key` = ?" ) )
        {
            statement.setInt( 2, account.getId() );
            statement.setString( 3, key );
            statement.setString( 1, value );
            statement.executeUpdate();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }
}
