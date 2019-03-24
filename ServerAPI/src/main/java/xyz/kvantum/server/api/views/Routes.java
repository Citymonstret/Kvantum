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
package xyz.kvantum.server.api.views;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.views.requesthandler.SimpleRequestHandler;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

/**
 * Utility method that aims to simplify the creation of simple routes
 */
@UtilityClass public final class Routes {

    /**
     * Register a GET route
     *
     * @param filter   Route filter
     * @param function Route generator
     */
    public static void get(@Nonnull @NonNull final String filter,
        @Nonnull @NonNull final BiConsumer<AbstractRequest, Response> function) {
        handle(filter, HttpMethod.GET, function);
    }

    /**
     * Register a POST route
     *
     * @param filter   Route filter
     * @param function Route generator
     */
    public static void post(@Nonnull @NonNull final String filter,
        @Nonnull @NonNull final BiConsumer<AbstractRequest, Response> function) {
        handle(filter, HttpMethod.POST, function);
    }

    private static void handle(@Nonnull @NonNull final String filter,
        @Nonnull @NonNull final HttpMethod method,
        @Nonnull @NonNull final BiConsumer<AbstractRequest, Response> function) {
        ServerImplementation.getImplementation().getRouter().add(
            SimpleRequestHandler.builder().generator(function).pattern(filter).httpMethod(method)
                .build());
    }

}
