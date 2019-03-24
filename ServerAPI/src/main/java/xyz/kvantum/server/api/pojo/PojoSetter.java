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
package xyz.kvantum.server.api.pojo;

import com.hervian.lambda.Lambda;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor final class PojoSetter<Pojo> {

    private final Lambda lambda;
    @Getter(AccessLevel.PACKAGE) private final Class<?> parameterType;

    public void set(@NonNull final Pojo instance, @NonNull final Object object) {
        if (parameterType.isPrimitive()) {
            if (parameterType.equals(int.class)) {
                lambda.invoke_for_void(instance, (int) object);
            } else if (parameterType.equals(long.class)) {
                lambda.invoke_for_void(instance, (long) object);
            } else if (parameterType.equals(float.class)) {
                lambda.invoke_for_void(instance, (float) object);
            } else if (parameterType.equals(boolean.class)) {
                lambda.invoke_for_void(instance, (boolean) object);
            } else if (parameterType.equals(double.class)) {
                lambda.invoke_for_void(instance, (double) object);
            } else if (parameterType.equals(byte.class)) {
                lambda.invoke_for_void(instance, (byte) object);
            } else if (parameterType.equals(char.class)) {
                lambda.invoke_for_void(instance, (char) object);
            }
        } else {
            lambda.invoke_for_void(instance, object);
        }
    }

}
