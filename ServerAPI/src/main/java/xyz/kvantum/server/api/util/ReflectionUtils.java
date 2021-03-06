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
package xyz.kvantum.server.api.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for dealing with reflection
 */
@UtilityClass final public class ReflectionUtils {

    /**
     * Generate a list of {@code @Annotated} methods from a class
     *
     * @param a     Annotation to search for
     * @param clazz Class in which the annotations are to be searched for
     * @return List containing the found annotations
     */
    public static <A extends Annotation> List<AnnotatedMethod<A>> getAnnotatedMethods(
        final Class<A> a, final Class<?> clazz) {
        Assert.notNull(a, clazz);

        final List<AnnotatedMethod<A>> annotatedMethods = new ArrayList<>();
        Class<?> c = clazz;
        while (c != Object.class) {
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(c.getDeclaredMethods()));

            allMethods.stream().filter(method -> method.isAnnotationPresent(a)).forEach(
                method -> annotatedMethods
                    .add(new AnnotatedMethod<>(method.getAnnotation(a), method)));

            c = c.getSuperclass();
        }

        return annotatedMethods;
    }

    /**
     * Value class for {@code @Annotated} methods
     */
    @Getter @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AnnotatedMethod<A extends Annotation> {

        private final A annotation;
        private final Method method;

    }

}
