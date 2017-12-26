/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2017 IntellectualSites
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
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.KvantumException;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;
import xyz.kvantum.server.api.validation.ValidationManager;
import xyz.kvantum.server.api.views.requesthandler.DebugMiddleware;
import xyz.kvantum.server.api.views.requesthandler.MiddlewareQueue;
import xyz.kvantum.server.api.views.requesthandler.MiddlewareQueuePopulator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A handler which uses an incoming
 * request to generate a response
 */
@EqualsAndHashCode(of = "uniqueId")
@SuppressWarnings("WeakerAccess")
public abstract class RequestHandler
{

    private static final Class[] REQUIRED_PARAMETERS = new Class[]{ AbstractRequest.class, Response.class };

    private final MiddlewareQueuePopulator middlewareQueuePopulator = new MiddlewareQueuePopulator();

    private final ValidationManager validationManager = new ValidationManager();
    private final Map<String, Lambda> alternateOutcomes = new HashMap<>();
    private final String uniqueId = UUID.randomUUID().toString();
    private final Collection<Decorator> decorators = new ArrayList<>();

    {
        if ( CoreConfig.debug )
        {
            ServerImplementation.getImplementation().log( "Adding DebugMiddleware to SimpleRequestHandler" );
            this.middlewareQueuePopulator.add( DebugMiddleware.class );
            try
            {
                this.registerAlternateOutcome( "debug", "handleDebug" );
            } catch ( final Throwable e )
            {
                e.printStackTrace();
            }
        }
    }

    public ValidationManager getValidationManager()
    {
        return validationManager;
    }

    public MiddlewareQueuePopulator getMiddlewareQueuePopulator()
    {
        return middlewareQueuePopulator;
    }

    /**
     * Register an alternate outcome, which can be triggered using Middleware
     * @param identifier Identifier used in {@link AbstractRequest#useAlternateOutcome(String)}
     * @param methodName Name of the method ( in the class, or any parent super classes )
     * @throws Exception If anything goes wrong
     */
    @SuppressWarnings("ALL")
    public void registerAlternateOutcome(@NonNull final String identifier,
                                         @NonNull final String methodName) throws Throwable
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
        final Lambda lambda = LambdaFactory.create( method );
        this.alternateOutcomes.put( identifier, lambda );
    }

    public void addResponseDecorator(@NonNull final Decorator decorator)
    {
        this.decorators.add( decorator );
    }

    public Optional<Lambda> getAlternateOutcomeMethod(final String identifier)
    {
        if ( alternateOutcomes.containsKey( identifier ) )
        {
            return Optional.of( alternateOutcomes.get( identifier ) );
        }
        return Optional.empty();
    }

    /**
     * Used to check if a request is to be served
     * by this RequestHandler
     *
     * @param request Incoming request
     * @return True if the request can be served by this handler
     * False if not
     */
    abstract public boolean matches(AbstractRequest request);

    /**
     * Simple alternate outcome for the {@link DebugMiddleware} middleware
     */
    @SuppressWarnings("unused")
    protected void handleDebug(final AbstractRequest request, final Response response)
    {
        ServerImplementation.getImplementation().log( "Using the handleDebug alternate outcome!" );
        response.copyFrom( generate( request ) );
    }

    final public Response handle(final AbstractRequest request)
    {
        Assert.isValid( request );

        final MiddlewareQueue middlewareQueue = middlewareQueuePopulator.generateQueue();
        middlewareQueue.handle( request );
        if ( !middlewareQueue.finished() )
        {
            ServerImplementation.getImplementation().log( "Skipping request as a middleware broke the chain!" );
            return null;
        }

        if ( request.hasMeta( AbstractRequest.ALTERNATE_OUTCOME ) )
        {
            //noinspection ConstantConditions
            final Optional<Lambda> method = getAlternateOutcomeMethod( request.getMeta( AbstractRequest
                    .ALTERNATE_OUTCOME ).toString() );
            if ( method.isPresent() )
            {
                final Lambda lambda = method.get();
                final Response response = new Response( this );
                lambda.invoke_for_void( this, request, response );
                return response;
            } else
            {
                throw new KvantumException( "Trying to access an internal redirect which isn't registered for type "
                        + this.getName() + ", identified by " + request.getMeta( AbstractRequest.ALTERNATE_OUTCOME ) );
            }
        }

        final Response response = generate( request );
        if ( response == null )
        {
            throw new KvantumException( "ResponseHandler (" + this.getName() + ") generated a null response" );
        }
        for ( final Decorator decorator : this.decorators )
        {
            decorator.decorate( response );
        }
        return response;
    }

    /**
     * Generate a response for the incoming request
     *
     * @param request The incoming request
     * @return The generated response
     */
    abstract public Response generate(final AbstractRequest request);

    /**
     * Get the view specific factory (if it exists)
     *
     * @param r Request IN
     * @return Null by default, or the ProviderFactory (if set by the view)
     */
    public ProviderFactory<? extends VariableProvider> getFactory(final AbstractRequest r)
    {
        return null;
    }

    /**
     * Get the unique internal name of the handler
     *
     * @return Handler name
     */
    abstract public String getName();

    /**
     * Indicate whether or not the request MUST
     * be served over HTTPS
     *
     * @return boolean indicated whether or not the request must be served
     * over HTTPS
     */
    public abstract boolean forceHTTPS();

}
