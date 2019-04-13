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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListMultiMap<K, V> {

    private final Map<K, List<V>> map;

    public ListMultiMap() {
        this(new HashMap<>());
    }

    public ListMultiMap(final Map<K, List<V>> map) {
        this.map = map;
    }

    public static <K, V> ListMultiMap<K, V> empty() {
        return new ListMultiMap<>(Collections.unmodifiableMap(new HashMap<>()));
    }

    public void put(final K key, final V value) {
        final List<V> collection = this.map.computeIfAbsent(key, k -> new ArrayList<>());
        collection.add(value);
    }

    public void clear() {
        this.map.clear();
    }

    public Collection<Map.Entry<K, V>> entries() {
        final Collection<Map.Entry<K, V>> entries = new ArrayList<>(getFullSize());
        for (final Map.Entry<K, List<V>> entry : this.map.entrySet()) {
            for (final V item : entry.getValue()) {
                entries.add(new AbstractMap.SimpleEntry<>(entry.getKey(), item));
            }
        }
        return entries;
    }

    public boolean containsEntry(final K key, final V value) {
        final Collection<V> values = this.get(key);
        if (values == null) {
            return false;
        }
        return values.contains(value);
    }

    public int getFullSize() {
        int size = 0;
        for (final List<V> list : this.map.values()) {
            size += list.size();
        }
        return size;
    }

    public List<V> get(final K key) {
        final List<V> list = map.get(key);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public void remove(final K key, final V value) {
        final List<V> values = this.get(key);
        if (values == null) {
            return;
        }
        values.remove(value);
    }

    public void remove(final K key) {
        this.map.remove(key);
    }

    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }

    public ListMultiMap<K, V> getCopy() {
        return new ListMultiMap<>(Collections.unmodifiableMap(this.map));
    }

}
