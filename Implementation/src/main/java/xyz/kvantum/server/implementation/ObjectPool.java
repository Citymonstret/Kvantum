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
package xyz.kvantum.server.implementation;

import java.util.concurrent.LinkedBlockingDeque;
import javax.annotation.Nullable;
import lombok.NonNull;
import xyz.kvantum.server.api.util.LambdaUtil;
import xyz.kvantum.server.api.util.Provider;

/**
 * An abstract pool of objects using a {@link LinkedBlockingDeque} backend
 *
 * @param <T> Pooled object type
 */
final class ObjectPool<T>
{

	private final LinkedBlockingDeque<T> availableObjects;

	/**
	 * Setup the handler with a specified number of instances
	 *
	 * @param objects Number of  instances (must be positive)
	 * @param supplier Supplier of objects
	 */
	ObjectPool(final int objects, @NonNull final Provider<T> supplier)
	{
		this.availableObjects = new LinkedBlockingDeque<>( objects );
		LambdaUtil.collectionAssign( () -> availableObjects, supplier, objects );
	}

	/**
	 * Poll the worker queue until an object is available. Warning: The thread will be locked until a new object is
	 * available
	 *
	 * @return The next available object
	 * @throws InterruptedException If the polling of the queue is interrupted
	 */
	private T getAvailable() throws InterruptedException
	{
		return this.availableObjects.takeFirst();
	}

	/**
	 * Get next available, but return null if the pooling is interrupted
	 *
	 * @return next object | null
	 */
	@Nullable final T getNullable()
	{
		try
		{
			return getAvailable();
		} catch ( final InterruptedException e )
		{
			return null; // Nullable
		}
	}

	/**
	 * Add an object back into the pool (after it has finished everything)
	 *
	 * @param t object
	 */
	final void add(@NonNull final T t)
	{
		this.availableObjects.add( t );
	}
}
