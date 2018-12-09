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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utility class for dealing with singleton instances
 */
@SuppressWarnings("ALL") @UtilityClass public class InstanceFactory {

    /**
     * Bind the given instance to a named static field.
     *
     * @param t         Instance
     * @param fieldName Field to bind to
     * @param <T>       Instance type
     */
    public static <T> void setupInstance(@Nonnull @NonNull final T t, @Nonnull @NonNull final String fieldName) {
        try {
            final Field field = t.getClass().getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(null, t);

            Assert.equals(field.get(null).equals(t), true);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Bind the given instance to a static field named "instance"
     *
     * @param t   Instance
     * @param <T> Instance type
     * @see #setupInstance(Object, String) To specify the field name
     */
    public static <T> void setupInstance(@Nonnull @NonNull final T t) {
        setupInstance(t, "instance");
    }

    /**
     * Bind the given instance all static fields matching the instance type.
     *
     * @param t   Instance
     * @param <T> Instance type
     */
    public static <T> void setupInstanceAutomagic(@Nonnull @NonNull final T t) {
        for (final Field field : t.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(t.getClass())) {
                try {
                    field.setAccessible(true);
                    if (field.get(null) != null) {
                        continue;
                    }
                    field.set(null, t);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
