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
package xyz.kvantum.server.api.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import lombok.NonNull;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.ReflectionUtils;
import xyz.kvantum.server.api.util.ReflectionUtils.AnnotatedMethod;

@SuppressWarnings( { "unused", "WeakerAccess" } )
public abstract class EventBus
{

	private final String name;
	private final boolean supportsAsync;

	public EventBus(final boolean supportsAsync)
	{
		this.name = this.getClass().getSimpleName();
		this.supportsAsync = supportsAsync;
	}

	public final boolean supportsAsync()
	{
		return this.supportsAsync;
	}

	public void registerListeners(@NonNull final Object listenerInstance)
	{
		final Class<?> clazz = listenerInstance.getClass();
		try
		{
			//
			// Find all methods annotated with @Listener
			//
			final Collection<AnnotatedMethod<Listener>> annotatedMethods =
					ReflectionUtils.getAnnotatedMethods( Listener.class, clazz );
			//
			// Create a collection of listener methods
			//
			final List<ListenerMethod> listenerMethods = new ArrayList<>( annotatedMethods.size() );
			for ( final AnnotatedMethod<Listener> annotatedMethod : annotatedMethods )
			{
				final Method method = annotatedMethod.getMethod();
				if ( method.getParameterCount() != 1 )
				{
					Logger.error( "Method {0} in {1} was annotated with @Listener, but does not have an event parameter. Skipping",
							method.getName(), clazz.getSimpleName() );
					continue;
				}
				final Class eventType = method.getParameterTypes()[ 0 ];
				final ListenerMethod listenerMethod = new ListenerMethod( method, listenerInstance, eventType );
				listenerMethods.add( listenerMethod );
			}
			//
			// Register each annotation internally
			//
			this.registerListenersInternally( listenerMethods );
		} catch ( final Throwable throwable )
		{
				throw new RuntimeException( String.format( "Failed to register listener of type %s",
					listenerInstance.getClass().getSimpleName() ), throwable );
		}
	}

	protected abstract void registerListenersInternally(@NonNull final Collection<ListenerMethod> listenerMethods);

	protected abstract <T> Future<T> throwAsync(@NonNull final T event);

	protected abstract <T> T throwSync(@NonNull final T event);

	public final <T> Future<T> throwEvent(@NonNull final T event, final boolean async)
	{
		if ( async && !this.supportsAsync() )
		{
			throw new IllegalStateException( "This event bus does not support asynchronous events" );
		}
		try
		{
			if ( async )
			{
				return this.throwAsync( event );
			} else
			{
				return CompletableFuture.completedFuture( this.throwSync( event ) );
			}
		} catch ( final Throwable throwable )
		{
			final RuntimeException exception = new RuntimeException( String.format( "Failed to handle event of type %s", event.getClass().getSimpleName() ),
					throwable );
			exception.printStackTrace();
			return CompletableFuture.failedFuture( exception );
		}
	}

	@Override public final int hashCode()
	{
		return this.toString().hashCode();
	}

	@Override public final boolean equals(final Object obj)
	{
		return obj != null && obj.getClass().equals( getClass() ) && ( ( EventBus ) obj ).name
				.equalsIgnoreCase( this.name );
	}

	@Override public final String toString()
	{
		return String.format( "EventBus{%s}", this.name );
	}
}
