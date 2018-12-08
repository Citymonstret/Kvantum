/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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

import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.util.AsciiString;

import javax.annotation.Nullable;

/**
 * Database handling for {@link ISession sessions}
 */
public interface ISessionDatabase {

    void setup() throws Exception;

    /**
     * Get the session load (used to verify session validity before letting the client access the session data)
     *
     * @param sessionID Session ID
     * @return session load if found, else null
     */
    @Nullable SessionLoad getSessionLoad(AsciiString sessionID);

    /**
     * Check if a session is valid
     *
     * @param session Session ID
     * @return the session load for the given session if valid, else null
     */
    @Nullable default SessionLoad isValid(final AsciiString session) {
        final SessionLoad sessionLoad = getSessionLoad(session);
        if (sessionLoad == null) {
            return null; // Nullable
        }
        final long difference = (System.currentTimeMillis() - sessionLoad.getLastActive()) / 1000;
        if (difference >= CoreConfig.Sessions.sessionTimeout) {
            if (CoreConfig.debug) {
                Message.SESSION_DELETED_OUTDATED.log(session);
            }
            deleteSession(session);
            return null; // Nullable
        }
        return sessionLoad;
    }

    /**
     * Store a session in the database
     *
     * @param session Session to store
     */
    void storeSession(ISession session);

    /**
     * Update a session in the database
     *
     * @param session Session ID to update
     */
    void updateSession(AsciiString session);

    /**
     * Delete a session from the database
     *
     * @param session Session ID to delete
     */
    void deleteSession(AsciiString session);

}
