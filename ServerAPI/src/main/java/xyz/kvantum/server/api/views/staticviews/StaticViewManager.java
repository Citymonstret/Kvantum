/*
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
package xyz.kvantum.server.api.views.staticviews;

import lombok.NonNull;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.IConsumer;
import xyz.kvantum.server.api.util.ReflectionUtils;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.requesthandler.Middleware;
import xyz.kvantum.server.api.views.staticviews.converters.StandardConverters;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final public class StaticViewManager
{

    private static final Class<?>[] parameters = new Class<?>[]{ AbstractRequest.class };
    private static final Class<?>[] alternativeParameters = new Class<?>[]{ AbstractRequest.class, Response.class };
    private static final Map<String, OutputConverter<?>> converters = new HashMap<>();

    static
    {
        StandardConverters.registerStandardConverters();
    }

    public static void generate(@NonNull final Object viewDeclaration) throws Exception
    {
        final Class<?> clazz = viewDeclaration.getClass();
        final List<ReflectionUtils.AnnotatedMethod<ViewMatcher>> annotatedMethods =
                ReflectionUtils.getAnnotatedMethods( ViewMatcher.class, clazz );
        ( (IConsumer<ReflectionUtils.AnnotatedMethod<ViewMatcher>>) annotatedMethod ->
        {
            final Method m = annotatedMethod.getMethod();
            final boolean usesAlternate = Arrays.equals( m.getParameterTypes(), alternativeParameters );
            final ViewMatcher matcher = annotatedMethod.getAnnotation();
            final ViewDeclaration declaration = new ViewDeclaration();

            if ( !usesAlternate && !Response.class.equals( m.getReturnType() ) && matcher.outputType().isEmpty() )
            {
                new IllegalArgumentException( m.getName() + " doesn't return response" ).printStackTrace();
            } else
            {
                if ( !usesAlternate && !Arrays.equals( m.getParameterTypes(), parameters ) )
                {
                    new IllegalArgumentException( "M has wrong parameter types" ).printStackTrace();
                } else
                {
                    declaration.setCache( matcher.cache() );
                    declaration.setFilter( matcher.filter() );
                    declaration.setMiddlewares( matcher.middlewares() );
                    declaration.setForceHttps( matcher.forceHTTPS() );
                    declaration.setHttpMethod( matcher.httpMethod() );
                    if ( !matcher.outputType().isEmpty() && converters.containsKey( matcher.outputType()
                            .toLowerCase() ) )
                    {
                        final OutputConverter<?> outputConverter = converters.get( matcher.outputType().toLowerCase() );
                        if ( !outputConverter.getClazz().equals( m.getReturnType() ) )
                        {
                            new IllegalArgumentException( m.getName() + " should return " + outputConverter.getClazz
                                    ().getSimpleName() ).printStackTrace();
                        } else
                        {
                            declaration.setOutputConverter( converters.get( matcher.outputType().toLowerCase() ) );
                        }
                    }
                    if ( matcher.name().isEmpty() )
                    {
                        declaration.setName( m.getName() );
                    } else
                    {
                        declaration.setName( matcher.name() );
                    }

                    RequestHandler view = null;
                    if ( matcher.cache() )
                    {
                        try
                        {
                            view = new CachedStaticView( declaration, new ResponseMethod( m, viewDeclaration,
                                    declaration.getOutputConverter() ) );
                        } catch ( Throwable throwable )
                        {
                            throwable.printStackTrace();
                        }
                    } else
                    {
                        try
                        {
                            view = new StaticView( declaration, new ResponseMethod( m, viewDeclaration,
                                    declaration.getOutputConverter() ) );
                        } catch ( Throwable throwable )
                        {
                            throwable.printStackTrace();
                        }
                    }

                    if ( view == null )
                    {
                        return;
                    }

                    for ( final Class<? extends Middleware> middleware : matcher.middlewares() )
                    {
                        view.getMiddlewareQueuePopulator().add( middleware );
                    }

                    ServerImplementation.getImplementation().getRouter().add( view );
                }
            }
        } ).foreach( annotatedMethods );
    }

    public static void registerConverter(final OutputConverter<?> converter)
    {
        converters.put( converter.getKey().toLowerCase(), converter );
    }

}
