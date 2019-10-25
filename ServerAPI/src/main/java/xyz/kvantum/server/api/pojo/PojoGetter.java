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

import com.esotericsoftware.reflectasm.MethodAccess;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;

@RequiredArgsConstructor final class PojoGetter<Pojo> {

    private final String name;
    private final MethodAccess methodAccess;
    private final int nameIndex;

    public Object get(final Pojo instance) {
        try {
            return methodAccess.invoke(instance, nameIndex);
        } catch (final AbstractMethodError e) {
            Logger.error("AbstractMethodError when getting field {}", name);
            ServerImplementation.getImplementation().getErrorDigest().digest(e);
        }
        throw new NullPointerException("No value retrieved for " + name);
    }
}
