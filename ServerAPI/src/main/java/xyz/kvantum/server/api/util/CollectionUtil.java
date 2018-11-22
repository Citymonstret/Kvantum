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

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Utility class for common {@link Collection collection} operations
 */
@UtilityClass @SuppressWarnings({ "unused", "WeakerAccess" }) public final class CollectionUtil
{

	/**
	 * Clear a collection and return the amount of cleared objects
	 *
	 * @param collection Collection to be cleared
	 * @return Size of collection before clearing
	 */
	public static int clear(@NonNull final Collection collection)
	{
		final int size = collection.size();
		collection.clear();
		return size;
	}

	/**
	 * Convert a list to a string list using {@link Object#toString()}
	 *
	 * @param collection Collection to be converted
	 * @param <T> Type
	 * @return list of strings
	 */
	public static <T> List<String> toStringList(@NonNull final Collection<T> collection)
	{
		final List<String> returnList = new ArrayList<>( collection.size() );
		collection.forEach( o -> returnList.add( o.toString() ) );
		return returnList;
	}

	/**
	 * Join all items in a list into a string, using a set delimiter
	 *
	 * @param collection Collection to be joined
	 * @param stringGenerator String generator
	 * @param joiner String that will be used to join the items
	 * @param <T> Type
	 * @return Joined string
	 * @see #join(Collection, String) for an {@link Object#toString()} implementation
	 */
	public static <T> String smartJoin(@NonNull final Collection<T> collection,
			@NonNull final Generator<T, String> stringGenerator, @NonNull final String joiner)
	{
		if ( collection.isEmpty() )
		{
			return "";
		}
		final StringBuilder stringBuilder = new StringBuilder();
		final Iterator<T> iterator = collection.iterator();
		while ( iterator.hasNext() )
		{
			stringBuilder.append( stringGenerator.generate( iterator.next() ) );
			if ( iterator.hasNext() )
			{
				stringBuilder.append( joiner );
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * Create a specific collection from a generic array
	 *
	 * @param creator Collection instance supplier
	 * @param array Array
	 * @return Created collection
	 */
	@SafeVarargs public static <T, C extends Collection<T>> C arrayToCollection(final Supplier<C> creator,
			final T... array)
	{
		return Arrays.stream( array ).collect( Collectors.toCollection( creator ) );
	}

	/**
	 * Compat utility. Use {@link StringList} directly instead.
	 *
	 * @deprecated Use {@link StringList}
	 */
	@Deprecated public static Collection<String> toStringCollection(@Nullable final String stringList)
	{
		return new StringList( stringList );
	}

	/**
	 * Join all items in a list into a string, using a set delimiter and {@link Object#toString()} as the string
	 * generator.
	 *
	 * @param collection Collection to be joined
	 * @param joiner String that will be used to join the items
	 * @param <T> Type
	 * @return Joined string
	 * @see #smartJoin(Collection, Generator, String) to customize the string generation behavior
	 */
	public static <T> String join(@NonNull final Collection<T> collection, @NonNull String joiner)
	{
		return smartJoin( collection, Object::toString, joiner );
	}

	/**
	 * Check if a string collection contains a string, ignoring string casing
	 *
	 * @param collection Collecting
	 * @param string String
	 * @return true if the collection contains the string, regardless of casing
	 */
	public static boolean containsIgnoreCase(@NonNull final Collection<? extends String> collection,
			@NonNull final String string)
	{
		if ( collection.isEmpty() )
		{
			return false;
		}
		for ( final String entry : collection )
		{
			if ( entry.equalsIgnoreCase( string ) )
			{
				return true;
			}
		}
		return false;
	}

}
