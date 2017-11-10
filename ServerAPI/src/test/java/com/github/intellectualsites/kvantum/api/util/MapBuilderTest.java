/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.util;

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
