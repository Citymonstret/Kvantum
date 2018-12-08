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

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility class for lambda based operations
 */
@SuppressWarnings("ALL") @UtilityClass public final class LambdaUtil {

    /**
     * Attempt to get the first object in a collection that matches the given predicate
     *
     * @param collection Collection
     * @param predicate  Predicate
     * @param <T>        Object type
     * @return Either the found object, or null
     */
    public static <T> Optional<T> getFirst(@NonNull final Collection<T> collection,
        @NonNull final Predicate<T> predicate) {
        return collection.stream().filter(predicate).findFirst();
    }

    /**
     * Attempt to get the first object in an array that matches the given predicate
     *
     * @param array     Array
     * @param predicate Predicate
     * @param <T>       Object type
     * @return Either the found object, or null
     */
    public static <T> Optional<T> getFirst(@NonNull final T[] array,
        @NonNull final Predicate<T> predicate) {
        return Arrays.stream(array).filter(predicate).findFirst();
    }

    /**
     * Create a new collection and assign a set number of objects to it
     *
     * @param collectionProvider Provider for the collection
     * @param valueProvider      Provider for the values
     * @param number             Number of items that should be assigned
     * @param <T>                Type
     * @return assigned collection
     */
    public static <T> Collection<T> collectionAssign(
        @NonNull final Provider<Collection<T>> collectionProvider,
        @NonNull final Provider<T> valueProvider, int number) {
        number = Assert.isPositive(number);

        final Collection<T> list = collectionProvider.provide();
        for (int i = 0; i < number; i++) {
            list.add(valueProvider.provide());
        }
        return list;
    }

    /**
     * Assign items to an array
     *
     * @param array    Array
     * @param provider Provider for the values
     * @param <T>      Type
     * @return assigned array
     */
    public static <T> T[] arrayAssign(@NonNull final T[] array,
        @NonNull final Provider<T> provider) {
        for (int i = 0; i < array.length; i++) {
            array[i] = provider.provide();
        }
        return array;
    }

    /**
     * Perform an action for each item in an array
     *
     * @param array    Array
     * @param consumer Action
     * @param <T>      Type
     */
    public static <T> void arrayForeach(@NonNull final T[] array,
        @NonNull final Consumer<T> consumer) {
        Arrays.stream(array).forEach(consumer);

    }

    /**
     * Perform an action for each item in an array (vararg...)
     *
     * @param consumer Action
     * @param array    Array
     * @param <T>      Type
     */
    @SafeVarargs public static <T> void arrayForeach(@NonNull final Consumer<T> consumer,
        final T... array) {
        Arrays.stream(array).forEach(consumer);
    }

    /**
     * Perform an action for every item in array, given that the item matches a predicate
     *
     * @param array    Array
     * @param filter   Predicate
     * @param consumer Consumer
     * @param <T>      Type
     */
    public static <T> void arrayForeach(@NonNull final T[] array,
        @NonNull final Predicate<T> filter, @NonNull final Consumer<T> consumer) {
        Assert.notNull(array, filter, consumer);

        Arrays.stream(array).filter(filter).forEach(consumer);
    }

}
