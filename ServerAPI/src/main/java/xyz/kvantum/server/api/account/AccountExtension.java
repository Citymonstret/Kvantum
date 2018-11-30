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
package xyz.kvantum.server.api.account;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;

@SuppressWarnings( { "unused", "WeakerAccess" } )
public abstract class AccountExtension
{

	private static final Map<Class<? extends AccountExtension>, Constructor<? extends AccountExtension>>
			CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

	private IAccount account;

	public void attach(@NonNull final IAccount account)
	{
		if ( this.account != null )
		{
			throw new IllegalStateException( "Cannot attach an extension twice" );
		}
		this.account = account;
		this.onAttach( account );
	}

	/**
	 * Executed when the extension has been attached to an account
	 *
	 * @param account Account that the instance is attached to
	 */
	protected abstract void onAttach(final IAccount account);

	/**
	 * Save the state of the extension (used when the account is unloaded)
	 */
	public abstract void saveState();

	public Optional<IAccount> getAccount()
	{
		return Optional.ofNullable( this.account );
	}

	public static <T extends AccountExtension> T createInstance(@NonNull final Class<T> clazz)
	{
		Constructor<T> constructor;
		try
		{
			constructor = getConstructor( clazz );
		} catch ( final NoSuchMethodException e )
		{
			throw new IllegalArgumentException( String.format( "Class \"%s\" does not have a no-args constructor",
					clazz.getSimpleName() ), e );
		}
		if ( !Modifier.isPublic( constructor.getModifiers() ) )
		{
			throw new IllegalArgumentException( String.format( "Class \"%s\" does not have a public no-args constructor",
					clazz.getSimpleName() ) );
		}
		T instance;
		try
		{
			instance = constructor.newInstance();
		} catch ( final Exception e )
		{
			throw new RuntimeException( "Failed to initialize extension", e );
		}
		return instance;
	}

	@SuppressWarnings( "ALL" )
	private static <T extends AccountExtension> Constructor<T> getConstructor(@NonNull final Class<T> clazz) throws NoSuchMethodException
	{
		final Constructor constructor = CONSTRUCTOR_CACHE.get( clazz );
		if ( constructor == null )
		{
			final Constructor<T> foundConstructor = clazz.getConstructor();
			CONSTRUCTOR_CACHE.put( clazz, foundConstructor );
			return foundConstructor;
		}
		return (Constructor<T>) constructor;
	}

}
