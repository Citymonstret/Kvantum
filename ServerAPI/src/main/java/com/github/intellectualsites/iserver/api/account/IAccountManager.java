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
package com.github.intellectualsites.iserver.api.account;

import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.session.ISession;
import com.github.intellectualsites.iserver.api.util.ApplicationStructure;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Manages {@link IAccount} and depends on {@link ApplicationStructure}
 * <p>
 * The global implementation can be retrieved using
 * {@link IntellectualServer#getApplicationStructure()} then {@link ApplicationStructure#getAccountManager()}
 * </p>
 */
public interface IAccountManager
{

    String SESSION_ACCOUNT_CONSTANT = "__user_id__";

    /**
     * Check if a given password matches the real password
     *
     * @param candidate Candidate password
     * @param password  Real password
     * @return true if the passwords are matching
     */
    static boolean checkPassword(final String candidate, final String password)
    {
        return BCrypt.checkpw( candidate, password );
    }

    /**
     * Get the container {@link ApplicationStructure}
     * @return {@link ApplicationStructure} implemenation
     */
    ApplicationStructure getApplicationStructure();

    /**
     * Setup the account manager
     * @throws Exception if anything goes wrong
     */
    void setup() throws Exception;

    /**
     * Create an {@link IAccount}
     * @param username Account username
     * @param password Account password
     * @return {@link Optional} containing the account if it was created successfully,
     *                          otherwise an empty optional ({@link Optional#empty()} is returned.
     */
    Optional<IAccount> createAccount(String username, String password);

    /**
     * Get an {@link IAccount} by username.
     * @param username Account username
     * @return {@link Optional} containing the account if it exsists
     *                          otherwise an empty optional ({@link Optional#empty()} is returned.
     */
    Optional<IAccount> getAccount(String username);

    /**
     * Get an {@link IAccount} by ID.
     * @param accountId Account ID
     * @return {@link Optional} containing the account if it exsists
     *                          otherwise an empty optional ({@link Optional#empty()} is returned.
     */
    Optional<IAccount> getAccount(int accountId);

    /**
     * Get an {@link IAccount} by session.
     * @param session session.
     * @return {@link Optional} containing the account if it exsists
     *                          otherwise an empty optional ({@link Optional#empty()} is returned.
     */
    default Optional<IAccount> getAccount(final ISession session)
    {
        if ( !session.contains( SESSION_ACCOUNT_CONSTANT ) )
        {
            return Optional.empty();
        }
        return getAccount( (int) session.get( SESSION_ACCOUNT_CONSTANT ) );
    }

    /**
     * Bind an {@link IAccount} to a {@link ISession}
     * @param account Account
     * @param session Session
     */
    default void bindAccount(final IAccount account, final ISession session)
    {
        session.set( SESSION_ACCOUNT_CONSTANT, account.getId() );
    }

    /**
     * Unbind any account from a {@link ISession}
     * @param session Session to be unbound
     */
    default void unbindAccount(final ISession session)
    {
        session.set( SESSION_ACCOUNT_CONSTANT, null );
    }

    /**
     * Set the data for an account
     * @param account Account
     * @param key Data key
     * @param value Data value
     */
    void setData(IAccount account, String key, String value);

    /**
     * Remove a data value from an account
     * @param account Account
     * @param key Data key
     */
    void removeData(IAccount account, String key);

    /**
     * Load the data into a {@link IAccount}
     * @param account Account to be loaded
     */
    void loadData(IAccount account);

    /**
     * Check if the admin account is created, otherwise a new admin account will be created
     * with credentials:
     * <ul>
     * <li><b>Username:</b> admin</li>
     * <li><b>Password:</b> admin</li>
     * </ul>
     */
    default void checkAdmin()
    {
        if ( !getAccount( "admin" ).isPresent() )
        {
            Optional<IAccount> adminAccount = createAccount( "admin", "admin" );
            if ( !adminAccount.isPresent() )
            {
                ServerImplementation.getImplementation().log( "Failed to create admin account :(" );
            } else
            {
                ServerImplementation.getImplementation().log( "Created admin account with password \"admin\"" );
                adminAccount.get().setData( "administrator", "true" );
            }
        }
    }
}
