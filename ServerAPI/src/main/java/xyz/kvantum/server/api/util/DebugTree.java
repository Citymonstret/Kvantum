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

import com.google.common.collect.Multimap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;

@Builder @EqualsAndHashCode public final class DebugTree
{

	private static final String NEW_LINE = System.lineSeparator();

	@NonNull private final String name;

	@Singular("entry") private final Map<String, Object> entries;

	public String toString()
	{
		final StringBuilder builder = new StringBuilder( "# " ).append( name ).append( NEW_LINE );
		for ( final Map.Entry<String, Object> entry : entries.entrySet() )
		{
			if ( entry.getValue() instanceof Map )
			{
				builder.append( "├── " ).append( entry.getKey() ).append( NEW_LINE );
				Map<?, ?> map = ( Map ) entry.getValue();
				final Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
				while ( iterator.hasNext() )
				{
					final Map.Entry e = iterator.next();
					if ( iterator.hasNext() )
					{
						builder.append( "|\t├── Key: " );
					} else
					{
						builder.append( "|\t└── Key: " );
					}
					builder.append( e.getKey() ).append( ", Value: " ).append( e.getValue() ).append( NEW_LINE );
				}
			} else if ( entry.getValue() instanceof Multimap )
			{
				builder.append( "├── " ).append( entry.getKey() ).append( NEW_LINE );
				Multimap<?, ?> map = ( Multimap<?, ?> ) entry.getValue();
				final Iterator<? extends Map.Entry<?, ?>> iterator = map.entries().iterator();
				while ( iterator.hasNext() )
				{
					final Map.Entry e = iterator.next();
					if ( iterator.hasNext() )
					{
						builder.append( "|\t├── Key: " );
					} else
					{
						builder.append( "|\t└── Key: " );
					}
					builder.append( e.getKey() ).append( ", Value: " ).append( e.getValue() ).append( NEW_LINE );
				}
			} else
			{
				builder.append( "├── " ).append( entry.getKey() ).append( ": " ).append( entry.getValue() )
						.append( NEW_LINE );
			}
		}
		builder.append( "└── End" );
		return builder.toString();
	}

	public Collection<String> collect()
	{
		return Arrays.asList( toString().split( NEW_LINE ) );
	}

}
