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
package xyz.kvantum.server.api.repository;

import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

/**
 * Matcher for queries
 *
 * @param <Q> Query Type
 * @param <V> Value type
 *            {@inheritDoc}
 */
@RequiredArgsConstructor public abstract class Matcher<Q, V> implements Predicate<V> {

    private final Q queryObject;

    abstract protected boolean matches(Q query, V value);

    /**
     * Check if an object matches the query
     *
     * @param value Value
     * @return True it there is a match
     */
    public final boolean matches(final V value) {
        return this.matches(queryObject, value);
    }

    @Override public final boolean test(final V value) {
        return this.matches(value);
    }

}
