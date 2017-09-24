/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.core;

/**
 * Use this class to manage the {@link IntellectualServer} instances
 */
public final class ServerImplementation
{

    private static IntellectualServer intellectualServer;

    /**
     * Register the server implementation used in the application instance
     * Cannot be used if the instance is already registered, use {@link #getImplementation()} to check for null.
     *
     * @param intellectualServer Server instance
     * @throws RuntimeException if the instance is already set
     */
    public static void registerServerImplementation(final IntellectualServer intellectualServer)
    {
        if ( ServerImplementation.intellectualServer != null )
        {
            throw new RuntimeException( "Trying to replace server implementation" );
        }
        ServerImplementation.intellectualServer = intellectualServer;
    }

    /**
     * Get the registered implementation
     * @return Implementation or mull
     */
    public static IntellectualServer getImplementation()
    {
        return intellectualServer;
    }

}
