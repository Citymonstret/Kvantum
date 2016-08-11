//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.config;

/**
 * The "official" configuration file
 * interface
 *
 * @author Citymonstret
 */
@SuppressWarnings("unused")
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
     * @param key   Value identifier
     * @param value The value itself
     * @param <T>   The value type
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
     * @param key   Value identifier
     * @param value Value
     * @param <T>   Value Type
     * @see #contains(String) To check if a value exists
     * @see #set(String, Object) To set a value
     */
    <T> void setIfNotExists(String key, T value);

}
