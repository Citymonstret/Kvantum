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
package xyz.kvantum.server.api.core;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Synchronized;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.views.RequestHandler;

@SuppressWarnings("unused") public final class WorkerProcedure
{

	private final List<WeakReference<WorkerProcedureInstance>> instances = new ArrayList<>();
	private volatile Map<String, StringHandler> handlers = new LinkedHashMap<>();

	/**
	 * Add a procedure, and allow the Worker to use it
	 *
	 * @param name Procedure name
	 * @param handler Procedure Handler
	 */
	@Synchronized public void addProcedure(@NonNull final String name, @NonNull final StringHandler handler)
	{
		this.handlers.put( name, handler );
		this.setChanged();
	}

	/**
	 * Add a procedure before another procedure. If there is no procedure with the specified name, it will be added to
	 * the last position of the queue
	 *
	 * @param name Procedure Name
	 * @param before Name of procedure that the current procedure should be placed before
	 * @param handler Procedure handler
	 */
	@Synchronized public void addProcedureBefore(final String name, final String before, final StringHandler handler)
	{
		Assert.notEmpty( name );
		Assert.notEmpty( before );
		Assert.notNull( handler );

		final Map<String, StringHandler> temporaryMap = new HashMap<>();
		for ( final Map.Entry<String, StringHandler> entry : handlers.entrySet() )
		{
			if ( entry.getKey().equalsIgnoreCase( before ) )
			{
				temporaryMap.put( name, handler );
			}
			temporaryMap.put( entry.getKey(), entry.getValue() );
		}
		if ( !temporaryMap.containsKey( name ) )
		{
			temporaryMap.put( name, handler );
		}
		this.handlers = temporaryMap;
		this.setChanged();
	}

	public boolean hasHandlers()
	{
		return !this.handlers.isEmpty();
	}

	/**
	 * Add a procedure after another procedure. If there is no procedure with the specified name, it will be added to
	 * the last position of the queue
	 *
	 * @param name Procedure Name
	 * @param after Name of procedure that the current procedure should be placed behind
	 * @param handler Procedure handler
	 */
	@Synchronized public void addProcedureAfter(final String name, final String after, final StringHandler handler)
	{
		Assert.notEmpty( name );
		Assert.notEmpty( after );
		Assert.notNull( handler );

		final Map<String, StringHandler> temporaryMap = new HashMap<>();
		for ( final Map.Entry<String, StringHandler> entry : handlers.entrySet() )
		{
			temporaryMap.put( entry.getKey(), entry.getValue() );
			if ( entry.getKey().equalsIgnoreCase( after ) )
			{
				temporaryMap.put( name, handler );
			}
		}
		if ( !temporaryMap.containsKey( name ) )
		{
			temporaryMap.put( name, handler );
		}
		this.handlers = temporaryMap;
		this.setChanged();
	}

	@Synchronized private void setChanged()
	{
		final Collection<StringHandler> stringHandlers = new ArrayList<>();

		for ( final Handler handler : handlers.values() )
		{
			if ( handler instanceof StringHandler )
			{
				stringHandlers.add( ( StringHandler ) handler );
			}
		}

		for ( final WeakReference<WorkerProcedureInstance> instanceReference : instances )
		{
			final WorkerProcedureInstance instance = instanceReference.get();
			if ( instance != null )
			{
				instance.stringHandlers = stringHandlers;
			}
		}
	}

	/**
	 * @return A new WorkerProcedureInstance
	 */
	public final WorkerProcedureInstance getInstance()
	{
		return new WorkerProcedureInstance();
	}

	public interface Handler<T>
	{

		T act(RequestHandler requestHandler, AbstractRequest request, T in);

		Class<T> getType();

	}

	@EqualsAndHashCode(of = "uniqueID") public static abstract class StringHandler implements Handler<String>
	{

		private final String uniqueID = UUID.randomUUID().toString();

		@Override public final Class<String> getType()
		{
			return String.class;
		}

	}

	/**
	 * An instance containing the handlers from the WorkerProcedure, that are split into Byte & String Handlers (to make
	 * them easier to use in the worker)
	 *
	 * The instance gets updated automatically when the WorkerProcedure is updated
	 */
	public final class WorkerProcedureInstance
	{

		private volatile Collection<StringHandler> stringHandlers = new ArrayList<>();
		private boolean containsHandlers;

		WorkerProcedureInstance()
		{
			instances.add( new WeakReference<>( this ) );
			this.stringHandlers.addAll( handlers.values() );
			this.containsHandlers = !stringHandlers.isEmpty();
		}

		public Collection<StringHandler> getStringHandlers()
		{
			return this.stringHandlers;
		}

		public boolean containsHandlers()
		{
			return hasHandlers();
		}
	}

}
