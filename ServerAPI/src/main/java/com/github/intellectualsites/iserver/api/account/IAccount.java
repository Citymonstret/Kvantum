package com.github.intellectualsites.iserver.api.account;

import java.util.Map;
import java.util.Optional;

/**
 * Account interface that is used throughout IntellectualServer,
 * This is suitable for use throughout web applications as well. See
 * {@link IAccountManager} for account management
 */
public interface IAccount
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

}
