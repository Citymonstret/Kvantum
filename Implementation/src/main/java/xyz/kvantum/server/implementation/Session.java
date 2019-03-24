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
package xyz.kvantum.server.implementation;

import lombok.*;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.VariableProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@EqualsAndHashCode(of = {"sessionKey"}) @SuppressWarnings("unused") public final class Session
    implements ISession, VariableProvider {

    @Getter private static final KvantumPojoFactory<ISession> kvantumPojoFactory =
        KvantumPojoFactory.forClass(ISession.class);
    private static long id = 0L;

    private final Map<String, Object> sessionStorage;

    @Getter private boolean deleted = false;

    @Setter @Getter private AsciiString sessionKey;

    Session() {
        this.sessionKey = AsciiString.randomUUIDAsciiString();
        this.sessionStorage = new ConcurrentHashMap<>();
        this.sessionStorage.put("id", "n/a");
    }

    @Override @Synchronized public boolean contains(final String variable) {
        return sessionStorage.containsKey(Assert.notNull(variable).toLowerCase(Locale.ENGLISH));
    }

    @Override @Synchronized public Object get(final String variable) {
        return sessionStorage.get(Assert.notNull(variable).toLowerCase(Locale.ENGLISH));
    }

    @Override public void setDeleted() {
        this.deleted = true;
    }

    @Override @Synchronized public Map<String, Object> getAll() {
        return new HashMap<>(sessionStorage);
    }

    @Override @Synchronized public ISession set(@NonNull final String s, @NonNull final Object o) {
        if (o == null) {
            sessionStorage.remove(s);
        } else {
            sessionStorage.put(s.toLowerCase(Locale.ENGLISH), o);
        }
        return this;
    }

    @Override public KvantumPojo<ISession> toKvantumPojo() {
        return kvantumPojoFactory.of(this);
    }

    @Override @SuppressWarnings("ALL") public <T> T getOrCompute(@NonNull final String key,
        @NonNull final Function<String, ? extends T> function) {
        final T object;
        if (!this.contains(key)) {
            object = function.apply(key);
            this.set(key, object);
        } else {
            object = (T) get(key);
        }
        return object;
    }

}
