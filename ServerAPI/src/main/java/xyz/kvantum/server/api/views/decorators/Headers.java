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
package xyz.kvantum.server.api.views.decorators;

import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.HeaderOption;
import xyz.kvantum.server.api.views.HeaderDecorator;

import java.util.Map;

/**
 * Decorator that will apply a set of headers
 * to a request
 */
public final class Headers extends HeaderDecorator
{

    private final Map<HeaderOption, String> headers;

    @SuppressWarnings("WeakerAccess")
    public Headers(final Map<HeaderOption, String> headers)
    {
        this.headers = ImmutableMap.copyOf( headers );
    }

    @Override
    public void decorate(@NonNull final Header header)
    {
        //
        // Only set the header if it isn't already set
        //
        headers.entrySet().stream().filter( entry -> !header.hasHeader( entry.getKey() ) )
                .forEach( entry -> header.set( entry.getKey(), entry.getValue() ) );
    }
}
