/*
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
package xyz.kvantum.server.api.core;

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
    public static void registerServerImplementation(final Kvantum intellectualServer)
    {
        if ( ServerImplementation.intellectualServer != null )
        {
            throw new KvantumException( "Trying to replace server implementation" );
        }
        ServerImplementation.intellectualServer = intellectualServer;
    }

    /**
     * Get the registered implementation
     * @return Implementation or mull
     */
    public static Kvantum getImplementation()
    {
        return intellectualServer;
    }

    public static boolean hasImplementation()
    {
        return getImplementation() != null;
    }

}
