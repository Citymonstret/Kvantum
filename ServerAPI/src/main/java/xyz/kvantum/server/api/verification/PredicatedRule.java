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
package xyz.kvantum.server.api.verification;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE) public final class PredicatedRule<T>
    implements Rule<T> {

    @NonNull private final String description;
    @NonNull private final Predicate<T> predicate;

    @Nonnull @Contract("_, _ -> new")
    public static <T> PredicatedRule<T> create(final String description,
        final Predicate<T> predicate) {
        return new PredicatedRule<>(description, predicate);
    }

    @Contract(pure = true) @Override public String getRuleDescription() {
        return this.description;
    }

    @Override public boolean test(final T t) {
        return this.predicate.test(t);
    }
}
