/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.views;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.exceptions.KvantumException;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;
import com.github.intellectualsites.kvantum.api.validation.ValidationManager;
import com.github.intellectualsites.kvantum.api.views.requesthandler.DebugMiddleware;
import com.github.intellectualsites.kvantum.api.views.requesthandler.MiddlewareQueue;
import com.github.intellectualsites.kvantum.api.views.requesthandler.MiddlewareQueuePopulator;
import lombok.EqualsAndHashCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A handler which uses an incoming
 * request to generate a response
 */
@EqualsAndHashCode(of = "uniqueId")
public abstract class RequestHandler
{

    private static final Class[] REQUIRED_PARAMETERS = new Class[]{ AbstractRequest.class, Response.class };

    protected final MiddlewareQueuePopulator middlewareQueuePopulator = new MiddlewareQueuePopulator();

    private final ValidationManager validationManager = new ValidationManager();
    private final Map<String, Method> alternateOutcomes = new HashMap<>();
    private final String uniqueId = UUID.randomUUID().toString();

    {
        if ( CoreConfig.debug )
        {
            ServerImplementation.getImplementation().log( "Adding DebugMiddleware to SimpleRequestHandler" );
            this.middlewareQueuePopulator.add( DebugMiddleware.class );
            try
            {
                this.registerAlternateOutcome( "debug", "handleDebug" );
            } catch ( Exception e )
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
    protected void registerAlternateOutcome(final String identifier, final String methodName) throws Exception
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
        method.setAccessible( true );
        this.alternateOutcomes.put( identifier, method );
    }

    public Optional<Method> getAlternateOutcomeMethod(final String identifier)
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
            final Optional<Method> method = getAlternateOutcomeMethod( request.getMeta( AbstractRequest.ALTERNATE_OUTCOME )
                    .toString() );
            if ( method.isPresent() )
            {
                final Method m = method.get();
                final Response response = new Response( this );
                try
                {
                    m.invoke( this, request, response );
                } catch ( IllegalAccessException | InvocationTargetException e )
                {
                    throw new KvantumException( "Failed to handle alternate outcome method", e );
                }
                return response;
            } else
            {
                throw new KvantumException( "Trying to access an internal redirect which isn't registered for type "
                        + this.getName() + ", identified by " + request.getMeta( AbstractRequest.ALTERNATE_OUTCOME ) );
            }
        }

        return generate( request );
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
    public ProviderFactory getFactory(final AbstractRequest r)
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
