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

import java.util.Map;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Utility class for dealing with (ANSI) log colours
 */
@UtilityClass public class ColorUtil
{

	private static final Map<Character, Integer> coloredMapping = MapBuilder.<Character, Integer>newHashMap()
			.put( '0', 30 ).put( '9', 34 ).put( 'c', 31 ).put( '2', 32 ).put( 'e', 78 ).put( '5', 35 ).put( 'f', 37 )
			.put( 'r', 0 ).get();

	/**
	 * Replace all &[color] codes in the given string
	 *
	 * @param in String to be replaced
	 * @return Colored formatted string
	 */
	public static String getReplaced(@NonNull String in)
	{
		for ( final Map.Entry<Character, Integer> entry : coloredMapping.entrySet() )
		{
			in = in.replace( "&" + entry.getKey(), "\u001B[" + entry.getValue() + ";1m" );
		}
		return in;
	}

	public static String getStripped(@NonNull String in)
	{
		for ( final char key : coloredMapping.keySet() )
		{
			in = in.replace( "&" + key, "" );
		}
		return in;
	}

}
