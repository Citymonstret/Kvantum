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
package xyz.kvantum.server.implementation.sqlite;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.implementation.Account;
import xyz.kvantum.server.implementation.SQLiteApplicationStructure;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

@RequiredArgsConstructor
final public class SQLiteAccountManager implements IAccountManager
{

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<IAccount> EMPTY_OPTIONAL = Optional.empty();

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
                "INTEGER PRIMARY KEY, username VARCHAR(64), password VARCHAR(255), CONSTRAINT " +
                "name_unique UNIQUE (username) )" );
        this.applicationStructure.getDatabaseManager().executeUpdate( "CREATE TABLE IF NOT EXISTS account_data ( id " +
                "INTEGER PRIMARY KEY, account_id INTEGER, `key` VARCHAR(255), `value` VARCHAR(255), UNIQUE" +
                "(account_id, `key`) )" );
        this.checkAdmin();
    }

    @Override
    public Optional<IAccount> createAccount(final IAccount temporary)
    {
        final String username = temporary.getUsername();
        final String password = temporary.getSuppliedPassword();

        Assert.notEmpty( username );
        Assert.notEmpty( password );

        Optional<IAccount> ret = EMPTY_OPTIONAL;
        if ( getAccount( username ).isPresent() )
        {
            return ret;
        }
        try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager()
                .prepareStatement( "INSERT INTO account(`username`, `password`) VALUES(?, ?)" ) )
        {
            statement.setString( 1, username );
            statement.setString( 2, hashPassword( password, getNewSalt() ) );
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

    @Override
    public Optional<IAccount> createAccount(final String username, final String password)
    {
        return this.createAccount( new Account( -1, username, password ) );
    }

    public Optional<IAccount> getAccount(final String username)
    {
        Assert.notEmpty( username );

        Optional<Integer> accountId = ServerImplementation.getImplementation().getCacheManager().getCachedId( username );
        Optional<IAccount> ret = EMPTY_OPTIONAL;
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
        ret.ifPresent( account -> account.setManager( this ) );
        return ret;
    }

    public Optional<IAccount> getAccount(final int accountId)
    {
        Optional<IAccount> ret = ServerImplementation.getImplementation().getCacheManager().getCachedAccount(
                accountId );
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
        ret.ifPresent( account -> account.setManager( this ) );
        return ret;
    }

    private IAccount getAccount(final ResultSet resultSet) throws Exception
    {
        final int id = resultSet.getInt( "id" );
        final String username = resultSet.getString( "username" );
        final String password = resultSet.getString( "password" );
        final IAccount account = new Account( id, username, password );
        account.setManager( this );
        return account;
    }

    @Override
    public void loadData(final IAccount account)
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
    public void removeData(IAccount account, String key)
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
    public void setData(IAccount account, String key, String value)
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
