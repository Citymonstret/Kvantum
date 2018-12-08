/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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

import static org.junit.jupiter.api.Assertions.*;

class StringListTest {

    private static final String TEST_STRING = "foo,bar,oof,rab";

    private static StringList createNewStringList() {
        return new StringList(TEST_STRING);
    }

    @Test void remove() {
        final StringList list = createNewStringList();
        final boolean result = list.remove("foo");
        assertTrue(result);
        assertFalse(list.contains("foo"));
    }

    @Test void addAll() {
        final StringList list = createNewStringList();
        final String toAdd = "cow,horse";
        assertTrue(list.addAll(toAdd));
        assertEquals(6, list.size());
        assertTrue(list.contains("cow"));
        assertTrue(list.contains("horse"));
    }

    @Test void add() {
        final StringList list = createNewStringList();
        final String toAdd = "cow";
        assertTrue(list.add(toAdd));
        assertEquals(5, list.size());
        assertTrue(list.contains("cow"));
    }

    @Test void testToString() {
        final StringList list = createNewStringList();
        assertEquals(TEST_STRING, list.toString());
    }

    @Test void toAsciiString() {
        final StringList list = createNewStringList();
        assertNotNull(list.toAsciiString());
    }

    @Test void size() {
        final StringList list = createNewStringList();
        assertEquals(4, list.size());
    }

    @Test void isEmpty() {
        final StringList list = createNewStringList();
        assertFalse(list.isEmpty());
        final StringList newList = new StringList("");
        assertTrue(newList.isEmpty());
    }

    @Test void contains() {
        final StringList list = createNewStringList();
        final boolean result = list.contains("foo");
        assertTrue(result);
    }

}
