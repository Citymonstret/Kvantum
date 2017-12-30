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
package xyz.kvantum.server.api.views.annotatedviews;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.CollectionUtil;
import xyz.kvantum.server.api.util.IConsumer;
import xyz.kvantum.server.api.util.ReflectionUtils;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.annotatedviews.converters.StandardConverters;
import xyz.kvantum.server.api.views.requesthandler.Middleware;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AnnotatedViewManager
{

    private final Class<?>[] parameters = new Class<?>[]{ AbstractRequest.class };
    private final Class<?>[] alternativeParameters = new Class<?>[]{ AbstractRequest.class, Response.class };
    private final Map<String, OutputConverter> converters = new HashMap<>();

    public AnnotatedViewManager()
    {
        StandardConverters.registerStandardConverters( this );
    }

    public <T> Collection<? extends RequestHandler> generate(@NonNull final T viewDeclaration) throws Exception
    {
        final Class<?> clazz = viewDeclaration.getClass();
        final List<ReflectionUtils.AnnotatedMethod<ViewMatcher>> annotatedMethods =
                ReflectionUtils.getAnnotatedMethods( ViewMatcher.class, clazz );
        final ImmutableCollection.Builder<RequestHandler> builder = ImmutableList.builder();
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
                            .toLowerCase( Locale.ENGLISH ) ) )
                    {
                        final OutputConverter outputConverter = converters.get( matcher.outputType().toLowerCase( Locale.ENGLISH ) );
                        if ( !outputConverter.getClasses().contains( m.getReturnType() ) )
                        {
                            new IllegalArgumentException( m.getName() + " should return one of "
                                    + CollectionUtil.smartJoin( outputConverter.getClasses(), Class::getSimpleName,
                                    ", " ) ).printStackTrace();
                        } else
                        {
                            declaration.setOutputConverter( converters.get( matcher.outputType().toLowerCase( Locale.ENGLISH ) ) );
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
                            view = new CachedAnnotatedView<>( declaration, new ResponseMethod<>( m, viewDeclaration,
                                    declaration.getOutputConverter() ) );
                        } catch ( Throwable throwable )
                        {
                            throwable.printStackTrace();
                        }
                    } else
                    {
                        try
                        {
                            view = new AnnotatedView<>( declaration, new ResponseMethod<>( m, viewDeclaration,
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

                    builder.add( view );
                }
            }
        } ).foreach( annotatedMethods );
        return builder.build();
    }

    @SuppressWarnings("WeakerAccess")
    public void registerConverter(@NonNull final OutputConverter converter)
    {
        converters.put( converter.getKey().toLowerCase( Locale.ENGLISH ), converter );
    }
}
