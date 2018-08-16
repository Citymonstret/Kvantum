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
package xyz.kvantum.server.api.matching;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.exceptions.NotImplementedException;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.annotatedviews.AnnotatedViewManager;

/**
 * Router that is responsible for {@link RequestHandler} matching
 */
@SuppressWarnings({ "WeakerAccess", "unused" }) public abstract class Router
{

	@Getter private final AnnotatedViewManager annotatedViewManager = new AnnotatedViewManager();

	/**
	 * Attempt to match a request to a {@link RequestHandler}
	 *
	 * @param request Request to be matched
	 * @return Depends on implementation, but should return either the matched {@link RequestHandler} or null, may also
	 * return a Status 404 View.
	 */
	public abstract RequestHandler match(AbstractRequest request);

	/**
	 * Add a new {@link RequestHandler} to the router
	 *
	 * @param handler RequestHandler that is to be registered
	 * @return The added {@link RequestHandler}
	 */
	public abstract <T extends RequestHandler> T add(T handler);

	/**
	 * Add a collection containing {@link RequestHandler RequestHandlers} to the router
	 *
	 * @param handlers RequestHandlers that are to be registered
	 */
	public final Collection<? extends RequestHandler> addAll(
			@NonNull final Collection<? extends RequestHandler> handlers)
	{
		handlers.forEach( this::add );
		return handlers;
	}

	/**
	 * Scan a class for {@link xyz.kvantum.server.api.views.annotatedviews.ViewMatcher} and register all constructed
	 * {@link xyz.kvantum.server.api.views.annotatedviews.AnnotatedView StaticViews}
	 *
	 * @param instance Instance to be scanned
	 */
	public final <T> Collection<? extends RequestHandler> scanAndAdd(@NonNull final T instance)
	{
		return this.addAll( this.scan( instance ) );
	}

	/**
	 * Scan a class for {@link xyz.kvantum.server.api.views.annotatedviews.ViewMatcher} and return a collection of
	 * constructed {@link xyz.kvantum.server.api.views.annotatedviews.AnnotatedView}
	 *
	 * @param instance Instance to be scanned
	 * @return Constructed views
	 */
	public final <T> Collection<? extends RequestHandler> scan(@NonNull final T instance)
	{
		try
		{
			return this.annotatedViewManager.generate( instance );
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	/**
	 * Attempts to remove a RequestHandler from the Router
	 *
	 * @param handler RequestHandler to be removed
	 */
	public abstract <T extends RequestHandler> void remove(T handler);

	/**
	 * Clear all handlers from the router
	 */
	public abstract void clear();

	/**
	 * Dump Router contents onto the server log
	 *
	 * @param server Server instance
	 */
	@SneakyThrows public void dump(@NonNull final Kvantum server)
	{
		throw new NotImplementedException( "Dump has not been overridden by the Router implementation" );
	}

	/**
	 * Get all registered {@link RequestHandler request handlers}
	 *
	 * @return Immutable collection containing all request handlers
	 */
	public abstract Collection<RequestHandler> getAll();
}
