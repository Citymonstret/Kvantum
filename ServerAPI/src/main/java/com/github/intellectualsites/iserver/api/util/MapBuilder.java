package com.github.intellectualsites.iserver.api.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("ALL")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final public class MapBuilder<K, V>
{

    private final Map<K, V> internalMap;
    private final Generator<Map<K, V>, Map<K, V>> generator;

    public static <K, V> MapBuilder<K, V> newHashMap()
    {
        return create( map -> new HashMap<>( map ), HashMap::new );
    }

    public static <K, V> MapBuilder<K, V> newLinkedHashMap()
    {
        return create( map -> new LinkedHashMap<>( map ), LinkedHashMap::new );
    }

    public static <K, V> MapBuilder<K, V> newTreeMap()
    {
        return create( map -> new TreeMap<>( map ), TreeMap::new );
    }

    private static <K, V> MapBuilder<K, V> create(final Generator<Map<K, V>,
            Map<K, V>> generator, final Provider<Map<K, V>> provider)
    {
        return new MapBuilder<>( provider.provide(), generator );
    }

    public MapBuilder<K, V> put(final K key, final V value)
    {
        this.internalMap.put( key, value );
        return this;
    }

    public MapBuilder<K, V> remove(final K key)
    {
        this.internalMap.remove( key );
        return this;
    }

    public Map<K, V> get()
    {
        return generator.generate( this.internalMap );
    }
}
