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
package xyz.kvantum.server.api.request;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import xyz.kvantum.server.api.util.Assert;

@SuppressWarnings("ALL") public enum HttpMethod
{

	/**
	 * Post requests are used to handle data
	 */
	POST,

	/**
	 * Get requests are handled for getting resources
	 */
	GET,

	/**
	 *
	 */
	PUT,

	PATCH,

	/**
	 * Retrieve the headers for a request Uses {@link #GET} but ignores any content
	 */
	HEAD( false ),

	/**
	 *
	 */
	DELETE,

	/**
	 * Used to indicate that ALL methods are applicable
	 */
	ALL;

	private static Map<String, HttpMethod> METHOD_CACHE;

	private final boolean hasBody;

	HttpMethod()
	{
		this( true );
	}

	HttpMethod(final boolean hasBody)
	{
		this.hasBody = hasBody;
	}

	public static Optional<HttpMethod> getByName(final String name)
	{
		Assert.notEmpty( name );

		if ( METHOD_CACHE == null )
		{
			METHOD_CACHE = new HashMap<>();
			for ( final HttpMethod method : values() )
			{
				METHOD_CACHE.put( method.name(), method );
			}
		}

		final String fixed = name.replaceAll( "\\s", "" ).toUpperCase( Locale.ENGLISH );
		return Optional.ofNullable( METHOD_CACHE.get( fixed ) );
	}

	public boolean hasBody()
	{
		return this.hasBody;
	}
}
