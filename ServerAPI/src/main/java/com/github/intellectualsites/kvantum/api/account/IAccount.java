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
package com.github.intellectualsites.kvantum.api.account;

import com.github.intellectualsites.kvantum.api.account.roles.AccountRole;
import com.github.intellectualsites.kvantum.api.logging.LogFormatted;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Account interface that is used throughout Kvantum,
 * This is suitable for use throughout web applications as well. See
 * {@link IAccountManager} for account management
 */
public interface IAccount extends LogFormatted
{

    /**
     * Get all data stored in the account
     * @return Map containing all data types
     */
    Map<String, String> getRawData();

    /**
     * Update metadata internally
     * @param key meta key
     * @param value meta value
     */
    void internalMetaUpdate(String key, String value);

    /**
     * Check if a provided password matches the account password
     * @param password Password to test
     * @return boolean indicating whether or not the provided password matches
     */
    boolean passwordMatches(String password);

    /**
     * Get account data for a specified key, if it exists
     * @param key Data key
     * @return Optional data value
     */
    Optional<String> getData(String key);

    /**
     * Update account data
     * @param key Data key
     * @param value Data value
     */
    void setData(String key, String value);

    /**
     * Remove a data value from the account
     * @param key Data key
     */
    void removeData(String key);

    /**
     * Get the (unique) account Id
     * @return Unique account ID
     */
    int getId();

    /**
     * Get the username associated with the account
     * @return Account username
     */
    String getUsername();

    /**
     * This method allows the {@link IAccountManager} to claim ownership of an account,
     * should only be used in the server implementation
     * @param manager Account owner
     */
    void setManager(IAccountManager manager);

    /**
     * Get all roles that the account has been assigned
     * @return collection containing all the user roles
     */
    Collection<AccountRole> getAccountRoles();

    /**
     * Add a role to the account
     * @param role Role
     */
    void addRole(AccountRole role);

    /**
     * Remove a role from the account
     * @param role role
     */
    void removeRole(AccountRole role);

    /**
     * Check if the account is permitted to perform an action
     * @param permissionKey Permission key
     * @return boolean indicating whether or not
     *         the account has the given permission
     */
    boolean isPermitted(String permissionKey);

    @Override
    default String getLogFormatted()
    {
        return String.format( "Account: { ID: %d, Username: %s }", getId(), getUsername() );
    }
}
