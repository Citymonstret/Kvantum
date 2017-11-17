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
