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
