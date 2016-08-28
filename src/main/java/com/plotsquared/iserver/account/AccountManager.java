package com.plotsquared.iserver.account;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.extra.ApplicationStructure;
import com.plotsquared.iserver.object.Session;
import com.plotsquared.iserver.util.Assert;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

@SuppressWarnings( "unused" )
public class AccountManager
{

    private static final String SESSION_ACCOUNT_CONSTANT = "__user_id__";

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Account> EMPTY_OPTIONAL = Optional.empty();

    private final ApplicationStructure applicationStructure;

    public AccountManager(final ApplicationStructure applicationStructure)
    {
        this.applicationStructure = applicationStructure;
    }

    private static String getNewSalt()
    {
        return BCrypt.gensalt();
    }

    private static String hashPassword(final String password, final String salt)
    {
        return BCrypt.hashpw( password, salt );
    }

    static boolean checkPassword(final String candidate, final String password)
    {
        return BCrypt.checkpw( candidate, password );
    }

    public ApplicationStructure getApplicationStructure()
    {
        return applicationStructure;
    }

    public void setup() throws Exception
    {
        this.applicationStructure.getDatabaseManager().executeUpdate( "CREATE TABLE IF NOT EXISTS account( id " +
                "INTEGER PRIMARY KEY, username VARCHAR(64), password VARCHAR(255), salt VARCHAR(255), CONSTRAINT " +
                "name_unique UNIQUE (username) )" );
        this.applicationStructure.getDatabaseManager().executeUpdate( "CREATE TABLE IF NOT EXISTS account_data ( id " +
                "INTEGER PRIMARY KEY, account_id INTEGER, `key` VARCHAR(255), `value` VARCHAR(255))" );
        if ( !getAccount( "admin" ).isPresent() )
        {
            Optional<Account> adminAccount = createAccount( "admin", "admin" );
            if ( !adminAccount.isPresent() )
            {
                Server.getInstance().log( "Failed to create admin account :(" );
            } else
            {
                Server.getInstance().log( "Created admin account with password \"admin\"" );
                adminAccount.get().setData( "administrator", "true" );
            }
        }
    }

    public Optional<Account> createAccount(final String username, final String password)
    {
        Assert.notEmpty( username );
        Assert.notEmpty( password );

        Optional<Account> ret = EMPTY_OPTIONAL;
        if ( getAccount( username ).isPresent() )
        {
            return ret;
        }
        try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager().prepareStatement(
                "INSERT INTO account(`username`, `password`, `salt`) VALUES(?, ?, ?)" ) )
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

        Optional<Integer> accountId = Server.getInstance().getCacheManager().getCachedId( username );
        Optional<Account> ret = EMPTY_OPTIONAL;
        if ( accountId.isPresent() )
        {
            ret = Server.getInstance().getCacheManager().getCachedAccount( accountId.get() );
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
        if ( ret.isPresent() )
        {
            Server.getInstance().getCacheManager().setCachedAccount( ret.get() );
        }
        return ret;
    }

    public Optional<Account> getAccount(final int accountId)
    {
        Optional<Account> ret = Server.getInstance().getCacheManager().getCachedAccount( accountId );
        if ( ret.isPresent() )
        {
            return ret;
        }
        try ( final PreparedStatement statement = this.applicationStructure.getDatabaseManager().prepareStatement(
                "SELECT * FROM `account` WHERE `id` = ?" ) )
        {
            statement.setInt( 1, accountId );
            statement.executeQuery();
            try ( final ResultSet resultSet = statement.getResultSet() )
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
        if ( ret.isPresent() )
        {
            Server.getInstance().getCacheManager().setCachedAccount( ret.get() );
        }
        return ret;
    }

    private Account getAccount(final ResultSet resultSet) throws Exception
    {
        final int id = resultSet.getInt( "id" );
        final String username = resultSet.getString( "username" );
        final String password = resultSet.getString( "password" );
        final String salt = resultSet.getString( "salt" );
        return new Account( id, username, password, salt, this );
    }

    public Optional<Account> getAccount(final Session session)
    {
        if ( !session.contains( SESSION_ACCOUNT_CONSTANT ) )
        {
            return Optional.empty();
        }
        return getAccount( (int) session.get( SESSION_ACCOUNT_CONSTANT ) );
    }

    public void bindAccount(final Account account, final Session session)
    {
        session.set( SESSION_ACCOUNT_CONSTANT, account.getId() );
    }

    public void unbindAccount(final Session session)
    {
        session.set( SESSION_ACCOUNT_CONSTANT, null );
    }

    void loadData(final Account account)
    {
        try ( final PreparedStatement statement = applicationStructure.getDatabaseManager().prepareStatement(
                "SELECT * FROM account_data WHERE account_id = ?" ))
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

    void removeData(Account account, String key)
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

    void setData(Account account, String key, String value)
    {
        try ( final PreparedStatement statement = applicationStructure.getDatabaseManager().prepareStatement(
                "INSERT INTO account_data(account_id, `key`, `value`) VALUES(?, ?, ?)" ) )
        {
            statement.setInt( 1, account.getId() );
            statement.setString( 2, key );
            statement.setString( 3, value );
            statement.executeUpdate();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }
}
