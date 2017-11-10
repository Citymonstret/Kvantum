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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.logging.LogWrapper;
import com.github.intellectualsites.kvantum.api.matching.Router;
import com.github.intellectualsites.kvantum.api.util.Assert;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.util.Optional;

/**
 * <p>
 * This class is used when initializing a new {@link ServerContext} instance.
 * </p>
 * <p>
 * ServerContext instances are created using {@link ServerContextBuilder}. A new
 * {@link ServerContextBuilder} can be initialized using either {@link ServerContext#builder()}
 * or by using {@code new ServerContextBuilder()}
 * </p>
 */
@Getter
@Builder
@SuppressWarnings( "WeakerAccess" )
public final class ServerContext
{

    @Builder.Default
    private boolean standalone = false;
    @NonNull
    private File coreFolder;
    @NonNull
    private LogWrapper logWrapper;
    @NonNull
    private Router router;

    /**
     * Creates a server instance using this context.
     * Will print any exceptions, but if the server
     * cannot be initialized a null optional will
     * be returned.
     */
    public final Optional<Kvantum> create()
    {
        Assert.equals( coreFolder.getAbsolutePath().indexOf( '!' ) == -1, true,
                "Cannot use a folder with '!' path as core folder" );
        Server server = null;
        try
        {
            server = new Server( this );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return Optional.ofNullable( server );
    }

}
