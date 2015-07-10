package com.intellectualsites.web.config;

/**
 * The "official" configuration file
 * interface
 *
 * @author Citymonstret
 */
public interface ConfigurationFile {

    /**
     * Reload the configuration file
     */
    void reload();

    /**
     * Save the configuration file
     */
    void saveFile();

    /**
     * Load the configuration file
     */
    void loadFile();

    /**
     * Set a value
     *
     * @param key Value identifier
     * @param value The value itself
     * @param <T> The value type
     */
    <T> void set(String key, T value);

    /**
     * Get a value
     *
     * @param key Value identifier
     * @param <T> Value type (wont cast - make sure this is correct)
     * @return value|null
     */
    <T> T get(String key);

    /**
     * Check if the configuration file contains a value
     *
     * @param key Value identifier
     * @return true|false
     */
    boolean contains(String key);

    /**
     * This will get the object, if it
     * exists, otherwise it returns the
     * default value (and sets the variable)
     *
     * @param key Key to search for
     * @param def Default Value
     * @param <T> Value type
     * @return value|def
     */
    <T> T get(String key, T def);

    /**
     * This will set a configuration value,
     * in the case that it doesn't exist.
     * Otherwise it will be ignored.
     *
     * @see #contains(String) To check if a value exists
     * @see #set(String, Object) To set a value
     *
     * @param key Value identifier
     * @param value Value
     * @param <T> Value Type
     */
    <T> void setIfNotExists(String key, T value);

}
