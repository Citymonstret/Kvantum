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
package xyz.kvantum.server.implementation;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.logging.LogWrapper;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.util.Assert;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;

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
@Setter
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
    @NonNull
    @Builder.Default
    private Function<ServerContext, SimpleServer> serverSupplier = SimpleServer::new;

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
        SimpleServer simpleServer = null;
        try
        {
            simpleServer = serverSupplier.apply( this );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        return Optional.ofNullable( simpleServer );
    }

}
