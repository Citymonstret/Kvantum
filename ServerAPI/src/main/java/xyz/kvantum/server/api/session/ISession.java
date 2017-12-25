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
package xyz.kvantum.server.api.session;

import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.VariableProvider;

import java.util.function.Function;

@SuppressWarnings("unused")
public interface ISession extends VariableProvider
{

    /**
     * Get the internal session key
     *
     * @return Session key
     */
    AsciiString getSessionKey();

    /**
     * Set the internal session key
     *
     * @param sessionKey Session key
     */
    void setSessionKey(AsciiString sessionKey);

    /**
     * Mark that the session is deleted
     */
    void setDeleted();

    /**
     * Check if the session is deleted
     *
     * @return True if the session is deleted
     */
    boolean isDeleted();

    /**
     * Set a session variable
     *
     * @param key   Variable key
     * @param value Variable value
     * @return This instance
     */
    ISession set(final String key, final Object value);

    /**
     * Convert the session to a {@link KvantumPojo} instance
     *
     * @return Converted object
     */
    KvantumPojo<ISession> toKvantumPojo();

    /**
     * Get an object, or compute a new object
     * if it isn't stored
     *
     * @param key      Variable key
     * @param function Function used to construct the object
     * @return the object
     */
    <T> T getOrCompute(String key, Function<String, ? extends T> function);
}
