package com.github.intellectualsites.kvantum.api.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

class CollectionUtilTest
{

    @Test
    void toStringList()
    {
        final Collection<Object> objects = Arrays.asList( 3, 8f, true );
        final Collection<?> converted = CollectionUtil.toStringList( objects );
        converted.forEach( object -> Assertions.assertTrue( object instanceof String ) );
    }

    @Test
    void clear()
    {
        final Collection<String> collection = new ArrayList<>( Arrays.asList( "heLlO", "WorLd" ) );
        int cleared = CollectionUtil.clear( collection );
        Assertions.assertEquals( cleared, 2 );
        Assertions.assertTrue( collection.isEmpty() );
    }

    @Test
    void smartJoin()
    {
        final Collection<Integer> collection = Arrays.asList( 3, 8, 6 );
        String joined = CollectionUtil.smartJoin( collection, i -> "{" + i + "}", ", " );
        Assertions.assertEquals( "{3}, {8}, {6}", joined );
    }

    @Test
    void join()
    {
        final Collection<Integer> collection = Arrays.asList( 3, 8, 6 );
        String joined = CollectionUtil.join( collection, ", " );
        Assertions.assertEquals( "3, 8, 6", joined );
    }

    @Test
    void containsIgnoreCase()
    {
        final Collection<String> collection = Arrays.asList( "heLlO", "WorLd" );
        Assertions.assertTrue( CollectionUtil.containsIgnoreCase( collection, "hello" ) );
        Assertions.assertTrue( CollectionUtil.containsIgnoreCase( collection, "WORLD" ) );
    }

}
