/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.core;

import lombok.NonNull;
import xyz.kvantum.server.api.exceptions.KvantumException;

/**
 * Use this class to manage the {@link Kvantum} instances
 */
public final class ServerImplementation
{

    private static Kvantum intellectualServer;

    /**
     * Register the server implementation used in the application instance
     * Cannot be used if the instance is already registered, use {@link #getImplementation()} to check for null.
     *
     * @param intellectualServer Server instance
     * @throws KvantumException if the instance is already set
     */
    public static void registerServerImplementation(@NonNull final Kvantum intellectualServer)
    {
        if ( ServerImplementation.intellectualServer != null )
        {
            throw new KvantumException( "Trying to replace server implementation" );
        }
        ServerImplementation.intellectualServer = intellectualServer;
    }

    /**
     * Get the registered implementation
     *
     * @return Implementation or mull
     */
    public static Kvantum getImplementation()
    {
        return intellectualServer;
    }

    /**
     * Check if an implementation has been registered
     *
     * @return True if an implementation has been registered
     */
    public static boolean hasImplementation()
    {
        return getImplementation() != null;
    }

}
