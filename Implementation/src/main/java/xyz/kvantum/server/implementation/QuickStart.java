/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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

import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.util.RequestManager;
import xyz.kvantum.server.api.views.RequestHandler;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("unused") @UtilityClass public final class QuickStart {

    /**
     * Utility method that creates a new simple server instance. It will register any request handlers passed as
     * arguments, and scan for annotations in the case that non request handler objects are passed
     *
     * @param classes Request handlers to register in the router
     * @return Created instance
     */
    @SuppressWarnings("ALL") public static Kvantum newStandaloneServer(final Object... classes)
        throws ServerStartFailureException {
        final ServerContext kvantumContext =
            ServerContext.builder().coreFolder(new File("./kvantum"))
                .router(RequestManager.builder().build())
                .standalone(true).serverSupplier(SimpleServer::new).build();
        final Optional<Kvantum> kvantumOptional = kvantumContext.create();
        if (!kvantumOptional.isPresent()) {
            throw new ServerStartFailureException(
                new IllegalStateException("Failed to create server instance"));
        }
        final Kvantum kvantum = kvantumOptional.get();
        for (final Object object : classes) {
            if (object == null) {
                throw new NullPointerException("Passed object is null. Not suitable for routing.");
            }
            if (object instanceof RequestHandler) {
                kvantum.getRouter().add((RequestHandler) object);
            } else {
                final Collection<? extends RequestHandler> added =
                    kvantum.getRouter().scanAndAdd(object);
                if (added.isEmpty()) {
                    throw new IllegalArgumentException("No views declarations found in " + object);
                }
            }
        }
        return kvantum;
    }

    public static final class ServerStartFailureException extends RuntimeException {

        ServerStartFailureException(final Exception e) {
            super(e);
        }

        ServerStartFailureException() {
            super();
        }

    }

}
