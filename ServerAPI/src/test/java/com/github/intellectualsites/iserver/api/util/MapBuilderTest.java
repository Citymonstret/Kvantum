package com.github.intellectualsites.iserver.api.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

class MapBuilderTest
{

    @Test
    void newHashMap()
    {
        final MapBuilder<String, String> mapBuilder = MapBuilder.newHashMap();
        Assertions.assertNotNull( mapBuilder );
        Assertions.assertTrue( mapBuilder.get() instanceof HashMap );
    }

    @Test
    void newLinkedHashMap()
    {
        final MapBuilder<String, String> mapBuilder = MapBuilder.newLinkedHashMap();
        Assertions.assertNotNull( mapBuilder );
        Assertions.assertTrue( mapBuilder.get() instanceof LinkedHashMap );
    }

    @Test
    void newTreeMap()
    {
        final MapBuilder<String, String> mapBuilder = MapBuilder.newTreeMap();
        Assertions.assertNotNull( mapBuilder );
        Assertions.assertTrue( mapBuilder.get() instanceof TreeMap );
    }

    @Test
    void put()
    {
        final MapBuilder<String, String> mapBuilder = MapBuilder.newHashMap();
        Assertions.assertNotNull( mapBuilder );
        mapBuilder.put( "Hello", "World" );
        Assertions.assertTrue( mapBuilder.get().containsKey( "Hello" ) );
        Assertions.assertTrue( mapBuilder.get().get( "Hello" ).equals( "World" ) );
    }

    @Test
    void remove()
    {
        final MapBuilder<String, String> mapBuilder = MapBuilder.newHashMap();
        Assertions.assertNotNull( mapBuilder );
        mapBuilder.put( "Hello", "World" );
        mapBuilder.remove( "Hello" );
        Assertions.assertFalse( mapBuilder.get().containsKey( "Hello" ) );
    }

}
