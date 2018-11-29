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

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.events.RequestHandlerAddedEvent;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.errors.View404;

/**
 * A simple {@link Router} implementation,
 */
@SuppressWarnings("unused") public final class RequestManager extends Router
{

	private static final Generator<AbstractRequest, RequestHandler> DEFAULT_404_GENERATOR = (request) -> View404
			.construct( request.getQuery().getFullRequest() );
	private static final Comparator<RequestHandler> REQUEST_HANDLER_COMPARATOR = Collections.reverseOrder( Comparator.comparing( RequestHandler::getMatchCount ) );

	@Builder.Default private List<RequestHandler> views = Collections.synchronizedList( new ArrayList<>() );
	@Setter @Getter @NonNull @Builder.Default private Generator<AbstractRequest, RequestHandler> error404Generator = DEFAULT_404_GENERATOR;
	private RequestHandler currentHead;
	private boolean changed = true;

	private final Timer timer;

	@Builder
	RequestManager()
	{
		this.timer = new Timer( true );
		System.out.println( "Delaying request manager timer initialization for 30 seconds." );
		this.timer.schedule( new TimerTask()
		{
			@Override public void run()
			{
				Logger.info( "Checking request handler sort rate..." );
				if ( CoreConfig.requestHandlerSortRate > 0 )
				{
					Logger.info( "Will sort request handlers every {}ms", CoreConfig.requestHandlerSortRate * 1000 );
					timer.schedule( new TimerTask()
					{
						@Override public void run()
						{
							sortViews();
						}
					}, 0L, CoreConfig.requestHandlerSortRate * 1000 );
				}
			}
		}, 30000L );
	}

	/**
	 * Register a view to the request manager
	 *
	 * @param view The view to register
	 * @return The request handler if it was created, null otherwise
	 */
	@Nullable @Override @SuppressWarnings("all") public RequestHandler add(@NonNull final RequestHandler view)
	{
		//
		// make sure the view pattern isn't registered yet
		//
		final Optional<RequestHandler> illegalRequestHandler = LambdaUtil
				.getFirst( views, v -> v.toString().equalsIgnoreCase( view.toString() ) );
		if ( illegalRequestHandler.isPresent() )
		{
			throw new IllegalArgumentException( "Duplicate view pattern: " + view.toString() );
		}

		//
		// Call event
		//
		final RequestHandlerAddedEvent requestHandlerAddedEvent = new RequestHandlerAddedEvent( view );
		ServerImplementation.getImplementation().getEventBus().emit( requestHandlerAddedEvent );
		if ( requestHandlerAddedEvent.isCancelled() )
		{
			return null; // Nullable
		}

		//
		// register handler
		//
		views.add( view );
		return view;
	}

	public void sortViews()
	{
		if ( !changed )
		{
			return;
		}

		Logger.info( "Sorting request handlers." );

		final long nanoTime1 = System.nanoTime();
		this.views.sort( REQUEST_HANDLER_COMPARATOR );
		this.currentHead = this.views.get( 0 );
		final long nanoTime2 = System.nanoTime();
		changed = false;

		Logger.info( "Sorted request handlers. Took {}ns", nanoTime2 - nanoTime1 );

		if ( CoreConfig.debug )
		{
			Logger.debug( "Request handler order:" );
			for ( int i = 0; i < this.views.size(); i++ )
			{
				final RequestHandler handler = this.views.get( i );
				Logger.debug( "- {0}: {1} - {2} matches", i + 1, handler, handler.getMatchCount() );
			}
		}
	}

	/**
	 * Try to find the request handler that matches the request
	 *
	 * @param request Incoming request
	 * @return Matching request handler, or {@link #getError404Generator()} if none was found
	 */
	@Override public RequestHandler match(final AbstractRequest request)
	{
		Assert.isValid( request );
		if ( !this.views.isEmpty() )
		{
			for ( final RequestHandler handler : this.views )
			{
				if ( handler.matches( request ) )
				{
					handler.incrementMatchCount();
					if ( currentHead == null || currentHead != handler )
					{
						changed = true;
					}
					return handler;
				}
			}
		}
		return error404Generator.generate( request );
	}

	@Override public void dump(@NonNull final Kvantum server)
	{
		( ( IConsumer<RequestHandler> ) view -> Message.REQUEST_HANDLER_DUMP
				.log( view.getClass().getSimpleName(), view.toString() ) ).foreach( views );
	}

	@Override public Collection<RequestHandler> getAll()
	{
		return ImmutableList.copyOf( this.views );
	}

	@Override public void remove(@NonNull final RequestHandler view)
	{
		if ( this.views.contains( view ) )
		{
			this.views.remove( view );
		} else
		{
			throw new IllegalArgumentException( "Cannot remove a view before registering it" );
		}
	}

	@Override public void clear()
	{
		Message.CLEARED_VIEWS.log( CollectionUtil.clear( this.views ) );
	}

}
