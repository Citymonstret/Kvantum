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
package xyz.kvantum.server.api.pojo;

import com.hervian.lambda.Lambda;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.logging.Logger;

@RequiredArgsConstructor final class PojoGetter<Pojo> {

    private final String name;
    private final Lambda lambda;
    private final Class<?> returnType;

    public Object get(@NonNull final Pojo instance) {
        try {
            if (returnType.isPrimitive()) {
                if (returnType.equals(int.class)) {
                    return lambda.invoke_for_int(instance);
                } else if (returnType.equals(long.class)) {
                    return lambda.invoke_for_long(instance);
                } else if (returnType.equals(float.class)) {
                    return lambda.invoke_for_float(instance);
                } else if (returnType.equals(boolean.class)) {
                    return lambda.invoke_for_boolean(instance);
                } else if (returnType.equals(double.class)) {
                    return lambda.invoke_for_double(instance);
                } else if (returnType.equals(byte.class)) {
                    return lambda.invoke_for_byte(instance);
                } else if (returnType.equals(char.class)) {
                    return lambda.invoke_for_char(instance);
                }
            }
            return lambda.invoke_for_Object(instance);
        } catch (final AbstractMethodError error) {
            Logger.error("AbstractMethodError when getting field {}", name);
            error.printStackTrace();
        }
        throw new NullPointerException("No value retrieved for " + name);
    }
}
