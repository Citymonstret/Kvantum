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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MapBuilderTest
{

	@Test void newHashMap()
	{
		final MapBuilder<String, String> mapBuilder = MapBuilder.newHashMap();
		Assertions.assertNotNull( mapBuilder );
		Assertions.assertTrue( mapBuilder.get() instanceof HashMap );
	}

	@Test void newLinkedHashMap()
	{
		final MapBuilder<String, String> mapBuilder = MapBuilder.newLinkedHashMap();
		Assertions.assertNotNull( mapBuilder );
		Assertions.assertTrue( mapBuilder.get() instanceof LinkedHashMap );
	}

	@Test void newTreeMap()
	{
		final MapBuilder<String, String> mapBuilder = MapBuilder.newTreeMap();
		Assertions.assertNotNull( mapBuilder );
		Assertions.assertTrue( mapBuilder.get() instanceof TreeMap );
	}

	@Test void put()
	{
		final MapBuilder<String, String> mapBuilder = MapBuilder.newHashMap();
		Assertions.assertNotNull( mapBuilder );
		mapBuilder.put( "Hello", "World" );
		Assertions.assertTrue( mapBuilder.get().containsKey( "Hello" ) );
		Assertions.assertTrue( mapBuilder.get().get( "Hello" ).equals( "World" ) );
	}

	@Test void remove()
	{
		final MapBuilder<String, String> mapBuilder = MapBuilder.newHashMap();
		Assertions.assertNotNull( mapBuilder );
		mapBuilder.put( "Hello", "World" );
		mapBuilder.remove( "Hello" );
		Assertions.assertFalse( mapBuilder.get().containsKey( "Hello" ) );
	}

}
