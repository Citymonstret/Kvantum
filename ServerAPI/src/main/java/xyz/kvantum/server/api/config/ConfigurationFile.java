/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.server.api.config;

import java.util.Map;

/**
 * The "official" configuration file interface
 */
@SuppressWarnings("unused")
public interface ConfigurationFile
{

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
     * Get all stored entries
     *
     * @return all entries
     */
    Map<String, Object> getAll();

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
