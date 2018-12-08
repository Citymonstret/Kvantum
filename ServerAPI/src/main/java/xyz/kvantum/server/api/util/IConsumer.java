/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
package xyz.kvantum.server.api.util;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Extension of {@link Consumer}
 *
 * @param <T> Type
 */
public interface IConsumer<T> extends Consumer<T> {

    default void foreach(final Predicate<T> predicate, final Collection<T> collection) {
        Assert.notNull(predicate, collection);

        collection.stream().filter(predicate).forEach(this);
    }

    default void foreach(final Collection<T> collection) {
        Assert.notNull(collection);

        collection.forEach(this);
    }

}
