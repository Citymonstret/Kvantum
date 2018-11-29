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
package xyz.kvantum.server.api.views;

import com.hervian.lambda.Lambda;
import com.hervian.lambda.LambdaFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.KvantumException;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;
import xyz.kvantum.server.api.validation.ValidationManager;
import xyz.kvantum.server.api.views.requesthandler.MiddlewareQueue;
import xyz.kvantum.server.api.views.requesthandler.MiddlewareQueuePopulator;

/**
 * The lowest-level class in the request handling chain. Allows for responses to be generated from incoming requests.
 * The class also contains utilities and managers that allow for the termination of invalid requests, and to change how
 * they are handled.
 */
@EqualsAndHashCode(of = "uniqueId") @SuppressWarnings({ "WeakerAccess", "unused" }) public abstract class RequestHandler
{

	private static final Class[] REQUIRED_PARAMETERS = new Class[] { AbstractRequest.class, Response.class };

	/**
	 * Manages middleware and produces queues that are then allowed to act on the request, and possibly terminate it, or
	 * redirect it to other handle methods ({@link #registerAlternateOutcome(String, String)}
	 */
	@Getter private final MiddlewareQueuePopulator middlewareQueuePopulator = new MiddlewareQueuePopulator();

	/**
	 * Manages validators and allows them to terminate the request based on certain criteria being fulfilled or not
	 */
	@Getter private final ValidationManager validationManager = new ValidationManager();
	private final Map<String, Lambda> alternateOutcomes = new HashMap<>();
	private final String uniqueId = UUID.randomUUID().toString();
	private final Collection<Decorator> decorators = new ArrayList<>();

	@Getter
	private long matchCount = 0L;

	/**
	 * Register an alternate outcome, which can be triggered using Middleware, by using {@link
	 * AbstractRequest#useAlternateOutcome(String)}, where the parameter is the identifier given to this method.
	 *
	 * @param identifier Identifier used in {@link AbstractRequest#useAlternateOutcome(String)}
	 * @param methodName Name of the method ( in the class, or any parent super classes )
	 * @throws Exception If anything goes wrong
	 */
	public void registerAlternateOutcome(@NonNull final String identifier, @NonNull final String methodName)
			throws Throwable
	{
		Assert.notEmpty( identifier );
		Assert.notEmpty( methodName );

		Method method = null;
		try
		{
			method = getClass().getDeclaredMethod( methodName, REQUIRED_PARAMETERS );
		} catch ( final NoSuchMethodException e )
		{
			Class<?> superClass = getClass();
			while ( ( superClass = superClass.getSuperclass() ) != Object.class )
			{
				try
				{
					method = superClass.getDeclaredMethod( methodName, REQUIRED_PARAMETERS );
					break;
				} catch ( final Exception ignored )
				{
				}
			}
		}
		if ( method == null )
		{
			throw new KvantumException( "Could not find #" + methodName + "( Request, Response )" );
		}
		this.alternateOutcomes.put( identifier, LambdaFactory.create( method ) );
	}

	/**
	 * Register a response decorator which will then be able to decorate the response after it has been generated
	 *
	 * @param decorator Decorator
	 */
	public void addResponseDecorator(@NonNull final Decorator decorator)
	{
		this.decorators.add( decorator );
	}

	/**
	 * Attempt to retrieve an alternate outcome method, based on the identifier given in {@link
	 * #registerAlternateOutcome(String, String)}
	 *
	 * @param identifier Method identifier
	 * @return Alternate outcome method, if present
	 */
	public Optional<Lambda> getAlternateOutcomeMethod(@NonNull final String identifier)
	{
		if ( alternateOutcomes.containsKey( identifier ) )
		{
			return Optional.of( alternateOutcomes.get( identifier ) );
		}
		return Optional.empty();
	}

	/**
	 * Increment the match counter by 1
	 */
	public void incrementMatchCount()
	{
		this.matchCount += 1;
	}

	/**
	 * Used to check if a request is to be served by this RequestHandler
	 *
	 * @param request Incoming request
	 * @return True if the request can be served by this handler False if not
	 */
	public abstract boolean matches(AbstractRequest request);

	/**
	 * Attempt to serve a request
	 *
	 * @param request Requested to serve
	 * @return Generated response
	 */
	@Nullable public final Response handle(final AbstractRequest request)
	{
		Assert.isValid( request );

		//
		// Allow middleware to act on the request
		//
		final MiddlewareQueue middlewareQueue = middlewareQueuePopulator.generateQueue();
		middlewareQueue.handle( request );
		if ( !middlewareQueue.finished() )
		{
			ServerImplementation.getImplementation().log( "Skipping request as a middleware broke the chain!" );
			return null; // Nullable
		}

		final Response response;

		//
		// If the middleware requested that an alternative
		// handling method should be used, we do that here
		//
		if ( request.hasMeta( AbstractRequest.ALTERNATE_OUTCOME ) )
		{
			//noinspection ConstantConditions
			final Optional<Lambda> method = getAlternateOutcomeMethod(
					request.getMeta( AbstractRequest.ALTERNATE_OUTCOME ).toString() );
			if ( method.isPresent() )
			{
				final Lambda lambda = method.get();
				response = new Response( this );
				lambda.invoke_for_void( this, request, response );
			} else
			{
				throw new KvantumException(
						"Trying to access an internal redirect which isn't registered for type " + this.getName()
								+ ", identified by " + request.getMeta( AbstractRequest.ALTERNATE_OUTCOME ) );
			}
		} else
		{
			//
			// Here we attempt to generate a response
			//
			response = generate( request );
		}

		//
		// If a null response is returned, panic
		//
		if ( response == null )
		{
			throw new KvantumException( "ResponseHandler (" + this.getName() + ") generated a null response" );
		}

		//
		// Decorators may decorate the responses
		//
		for ( final Decorator decorator : this.decorators )
		{
			decorator.decorate( response );
		}

		//
		// Return
		//
		return response;
	}

	/**
	 * Generate a response for the incoming request
	 *
	 * @param request The incoming request
	 * @return The generated response
	 */
	@Nullable abstract public Response generate(final AbstractRequest request);

	/**
	 * Get the view specific factory (if it exists)
	 *
	 * @param r Request IN
	 * @return Null by default, or the ProviderFactory (if set by the view)
	 */
	@Nullable public ProviderFactory<? extends VariableProvider> getFactory(final AbstractRequest r)
	{
		return null; // Nullable
	}

	/**
	 * Get the unique internal name of the handler
	 *
	 * @return Handler name
	 */
	abstract public String getName();

	/**
	 * Indicate whether or not the request MUST be served over HTTPS
	 *
	 * @return boolean indicated whether or not the request must be served over HTTPS
	 */
	public abstract boolean forceHTTPS();

}
