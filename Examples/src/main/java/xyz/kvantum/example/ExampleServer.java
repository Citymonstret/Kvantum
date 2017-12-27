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
package xyz.kvantum.example;

import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.RequestManager;
import xyz.kvantum.server.implementation.DefaultLogWrapper;
import xyz.kvantum.server.implementation.ServerContext;
import xyz.kvantum.server.implementation.SimpleServer;
import xyz.kvantum.server.implementation.StandaloneServer;

import java.io.File;
import java.util.Optional;

/**
 * Example that will started a {@link Kvantum} instance,
 * implemented by {@link SimpleServer}.
 * <p>
 * This example will also load {@link HelloWorld}, {@link ExampleLogin}
 * &amp; {@link ExampleSession}
 */
public final class ExampleServer
{

    public static void main(final String[] args)
    {
        final ServerContext serverContext = ServerContext.builder()
                .coreFolder( new File( "exampleServer" ) )
                .logWrapper( new DefaultLogWrapper() )
                .router( RequestManager.builder().build() )
                .standalone( true )
                .serverSupplier( StandaloneServer::new )
                .build();
        final Optional<Kvantum> serverOptional = serverContext.create();
        if ( serverOptional.isPresent() )
        {
            new HelloWorld(); // Initialize Hello World examples
            new ExampleLogin(); // Initialize the login example
            new ExampleSession();
            new ExampleSearch();
            new ExampleApi();
            serverOptional.get().start();
            Logger.info( "Server started successfully!" );
        } else
        {
            System.out.println( "ERROR: Could not create server..." );
        }
    }

}
