/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.kvantum.server.api.util.AsciiString.of;

class AsciiStringTest
{

    @Test
    void isEmpty()
    {
        final AsciiString asciiString1 = of( "", false );
        assertTrue( asciiString1.isEmpty() );
        final AsciiString asciiString2 = of( "content", false );
        assertFalse( asciiString2.isEmpty() );
    }

    @Test
    void getValue()
    {
        final AsciiString asciiString1 = of( "Hello World", false );
        final String string = "Hello World";
        assertTrue( Arrays.equals( asciiString1.getValue(), string.getBytes( StandardCharsets.US_ASCII ) ) );
    }

    @Test
    void length()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertEquals( 11, asciiString.length() );
    }

    @Test
    void charAt()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertEquals( 'l', asciiString.charAt( 3 ) );
    }

    @Test
    void subSequence()
    {
        final AsciiString asciiString = of( "Hello World", false );
        final CharSequence subsequence = asciiString.subSequence( 0, 5 );
        assertEquals( "Hello", subsequence );
    }

    @Test
    void testToString()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertEquals( "Hello World", asciiString.toString() );
    }

    @Test
    void testHashCode()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertEquals( "Hello World".hashCode(), asciiString.hashCode() );
    }

    @Test
    void equals()
    {
        final AsciiString asciiString1 = of( "Hello World", false );
        final AsciiString asciiString2 = of( "Hello World", false );
        assertTrue( asciiString1.equals( asciiString2 ) );
        final String string = "Hello World";
        assertTrue( asciiString1.equals( string ) );
    }

    @Test
    void contains()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertTrue( asciiString.contains( "Hello" ) );
        assertFalse( asciiString.contains( "Obama" ) );
    }

    @Test
    void containsIgnoreCase()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertTrue( asciiString.containsIgnoreCase( "HELLO" ) );
        assertTrue( asciiString.containsIgnoreCase( "hello" ) );
        assertTrue( asciiString.containsIgnoreCase( "hElLo" ) );
    }

    @Test
    void equalsIgnoreCase()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertTrue( asciiString.equalsIgnoreCase( "hElLO woRlD" ) );
    }

    @Test
    void endsWith()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertTrue( asciiString.endsWith( "World" ) );
    }

    @Test
    void toLowerCase()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertTrue( asciiString.toLowerCase().equals( "hello world" ) );
    }

    @Test
    void toUpperCase()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertTrue( asciiString.toUpperCase().equals( "HELLO WORLD" ) );
    }

    @Test
    void compareTo()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertEquals( "Hello World".compareTo( "Hello" ), asciiString.compareTo( "Hello" ) );
    }

    @Test
    void split()
    {
        final AsciiString asciiString = of( "Hello World", false );
        final Collection<AsciiString> collection = asciiString.split( " " );
        assertEquals( 2, collection.size() );
    }

    @Test
    void startsWith()
    {
        final AsciiString asciiString = of( "Hello World", false );
        assertTrue( asciiString.startsWith( "Hello" ) );
    }

}
