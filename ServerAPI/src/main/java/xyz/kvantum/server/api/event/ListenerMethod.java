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
package xyz.kvantum.server.api.event;

import com.esotericsoftware.reflectasm.MethodAccess;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter @SuppressWarnings({"WeakerAccess"}) public final class ListenerMethod {

    private final MethodAccess methodAccess;
    private final int nameIndex;
    private final Class eventType;
    private final Object instance;

    public ListenerMethod(final Method method, final Object instance, final Class eventType) {
        this.eventType = eventType;
        this.instance = instance;
        this.methodAccess = MethodAccess.get(method.getDeclaringClass());
        this.nameIndex = methodAccess.getIndex(method.getName());
    }

    public void invoke(final Object instance) {
        if (!instance.getClass().equals(eventType)) {
            throw new IllegalArgumentException(String.
                format("Mis-matched event types. Requires '%s', but was given '%s'",
                    eventType.getSimpleName(), instance.getClass().getSimpleName()));
        }
        this.methodAccess.invoke(this.instance, nameIndex, instance);
    }

    @Override public boolean equals(final Object obj) {
        return (obj != null && obj.getClass().equals(this.getClass()) && obj.toString()
            .equals(this.toString()));
    }

    @Override public int hashCode() {
        return this.toString().hashCode();
    }

    @Override public String toString() {
        // type-event_type
        return String
            .format("%s-%s", instance.getClass().getSimpleName(), eventType.getSimpleName());
    }

}
