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
package com.plotsquared.iserver.extra.accounts;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.crush.syntax.ProviderFactory;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Session;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.SQLiteManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AccountManager implements ProviderFactory<Account>
{

    private final List<Account> accountList;
    private final ConcurrentHashMap<Session, Account> sessionAccountMap;
    private final SQLiteManager databaseManager;
    private int id = 0;

    public AccountManager(final SQLiteManager databaseManager)
    {
        Assert.notNull( databaseManager );

        this.accountList = Collections.synchronizedList( new ArrayList<Account>() );
        this.sessionAccountMap = new ConcurrentHashMap<>();
        this.databaseManager = databaseManager;
    }

    public boolean load()
    {
        try
        {
            databaseManager.executeUpdate( "CREATE TABLE IF NOT EXISTS account(id INTEGER PRIMARY KEY, name VARCHAR" +
                    "(32), password VARCHAR(256), `salt` VARCHAR(256))" );
            PreparedStatement loadAccounts = databaseManager.prepareStatement( "SELECT * FROM account" );
            ResultSet results = loadAccounts.executeQuery();
            while ( results.next() )
            {
                this.id++;
                int id = results.getInt( "id" );
                String username = results.getString( "name" );
                String password = results.getString( "password" );
                String salt = results.getString( "salt" );
                Account account = new Account( id, username, password.getBytes(), salt.getBytes() );
                registerAccount( account );
            }
            results.close();
            loadAccounts.close();

            Server.getInstance().log( "Loaded " + accountList.size() + " accounts from the database" );

            if ( accountList.isEmpty() )
            {
                final byte[] salt = PasswordUtil.getSalt();
                final byte[] encryptedPassword = PasswordUtil.encryptPassword( "test", salt );
                createAccount( new Account( getNextId(), "test", encryptedPassword, salt ) );
            }
        } catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int getNextId()
    {
        return id++;
    }

    public void createAccount(final Account account) throws SQLException
    {
        Assert.notNull( account );

        PreparedStatement statement = databaseManager.prepareStatement( "INSERT INTO account(name, password, `salt`) " +
                "VALUES(?, ?, ?)" );
        statement.setString( 1, account.getUsername() );
        statement.setString( 2, new String( account.getPassword() ) );
        statement.setString( 3, new String( account.getSalt() ) );
        statement.executeUpdate();
        statement.close();
        registerAccount( account );
    }

    public synchronized Account getAccount(Session session)
    {
        Assert.notNull( session );

        if ( !sessionAccountMap.containsKey( session ) )
        {
            return null;
        }
        return sessionAccountMap.get( session );
    }

    public Account getAccount(final Object[] objects)
    {
        // 0 = id
        // 1 = username
        // 2 = email
        // ...
        Assert.equals( objects.length >= 2, true );

        if ( objects[ 0 ] != null )
        {
            return getAccountById( (Integer) objects[ 0 ] );
        }
        if ( objects[ 1 ] != null )
        {
            return getAccountByUsername( objects[ 1 ].toString() );
        }
        return null;
    }

    public Account getAccountById(final int id)
    {
        Assert.isPositive( id );

        for ( final Account account : accountList )
        {
            if ( account.getID() == id )
            {
                return account;
            }
        }
        return null;
    }

    public Account getAccountByUsername(final String username)
    {
        Assert.notEmpty( username );

        for ( final Account account : accountList )
        {
            if ( account.getUsername().equals( username ) )
            {
                return account;
            }
        }
        return null;
    }

    public void unbindAccount(final Session session)
    {
        Assert.notNull( session );

        if ( sessionAccountMap.containsKey( session ) )
        {
            sessionAccountMap.remove( session );
        }
    }

    public synchronized void bindAccount(final Session session, final Account account)
    {
        Assert.notNull( session, account );

        sessionAccountMap.put( session, account );
    }

    public void registerAccount(final Account account)
    {
        Assert.notNull( account );

        accountList.add( account );
    }

    public Session getSession(final Account account)
    {
        Assert.notNull( account );

        if ( !sessionAccountMap.containsValue( account ) )
        {
            return null;
        }
        for ( Session session : sessionAccountMap.keySet() )
        {
            if ( sessionAccountMap.get( session ).equals( account ) )
            {
                return session;
            }
        }
        return null;
    }

    @Override
    public Account get(Request r)
    {
        return getAccount( r.getSession() );
    }

    @Override
    public String providerName()
    {
        return "account";
    }
}
