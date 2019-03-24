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
package xyz.kvantum.server.api.views.annotatedviews;

import lombok.Getter;
import lombok.NonNull;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.CollectionUtil;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;

public abstract class OutputConverter {

    @Getter private final String key;
    @Getter private final Collection<Class> classes;

    protected OutputConverter(@Nonnull @NonNull final String key,
        @Nonnull @NonNull final Class<?>... classes) {
        this.key = key;
        this.classes = CollectionUtil.arrayToCollection(HashSet::new, classes);
    }

    protected abstract Response generateResponse(final Object input);

}
