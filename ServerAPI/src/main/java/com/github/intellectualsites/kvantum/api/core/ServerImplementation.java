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
package com.github.intellectualsites.kvantum.api.core;

import com.github.intellectualsites.kvantum.api.exceptions.KvantumException;

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
