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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for dealing with common {@link Map} operations
 */
@UtilityClass public final class MapUtil {

    /**
     * Convert a map with a certain type to another type, given that the keys are Strings
     *
     * @param input     Inputted map
     * @param converter Converter, that will convert the type of the inputted map into the output type
     * @param <I>       Input type
     * @param <O>       Output type
     * @return Converted map
     */
    @Nonnull public static <I, O> Map<String, O> convertMap(
        @Nonnull @NonNull final Map<String, I> input, @NonNull final Converter<I, O> converter) {
        final Map<String, O> output = new HashMap<>(input.size());
        input.forEach((key, value) -> output.put(key, converter.convert(value)));
        return output;
    }

    /**
     * Join a map together into a string.
     * <pre>{@code
     * Map<String, String> map = new HashMap<>();
     * map.put("hello", "world");
     * map.put("goodbye", "new york");
     * String joined = join( map, " = ", "," );
     * // joined = "hello = world, goodbye = new york"
     * }</pre>
     *
     * @param map       Map to be joined
     * @param combiner  String sequence used to combine the key and the value
     * @param separator String sequence used to separate map entries
     * @param <K>       Key type
     * @param <V>       Value type
     * @return joined string
     */
    @Nonnull public static <K, V> String join(@Nonnull @NonNull final Map<K, V> map,
        @Nonnull @NonNull final String combiner, @Nonnull @NonNull final String separator) {
        if (map.isEmpty()) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        final Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<K, V> entry = iterator.next();
            builder.append(entry.getKey()).append(combiner).append(entry.getValue());
            if (iterator.hasNext()) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    /**
     * Helper interface for {@link #convertMap(Map, Converter)}
     *
     * @param <I> Input type
     * @param <O> Output type
     */
    @FunctionalInterface public interface Converter<I, O> {

        /**
         * Convert an input to another type
         *
         * @param input Input to be converted
         * @return Converted object
         */
        O convert(I input);

    }

}
