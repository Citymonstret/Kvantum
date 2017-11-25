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

import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.IConsumer;
import xyz.kvantum.server.api.util.ReflectionUtils;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.requesthandler.Middleware;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

final public class StaticViewManager
{

    private static final Class<?>[] parameters = new Class<?>[]{ AbstractRequest.class };
    private static final Class<?>[] alternativeParameters = new Class<?>[]{ AbstractRequest.class, Response.class };

    public static void generate(final Object viewDeclaration) throws Exception
    {
        final Class<?> clazz = viewDeclaration.getClass();
        final List<ReflectionUtils.AnnotatedMethod<ViewMatcher>> annotatedMethods =
                ReflectionUtils.getAnnotatedMethods( ViewMatcher.class, clazz );
        ( (IConsumer<ReflectionUtils.AnnotatedMethod<ViewMatcher>>) annotatedMethod ->
        {
            final Method m = annotatedMethod.getMethod();
            final boolean usesAlternate = Arrays.equals( m.getParameterTypes(), alternativeParameters );

            if ( !usesAlternate && !Response.class.equals( m.getReturnType() ) )
            {
                new IllegalArgumentException( "M doesn't return response" ).printStackTrace();
            } else
            {
                if ( !usesAlternate && !Arrays.equals( m.getParameterTypes(), parameters ) )
                {
                    new IllegalArgumentException( "M has wrong parameter types" ).printStackTrace();
                } else
                {
                    final ViewMatcher matcher = annotatedMethod.getAnnotation();
                    final RequestHandler view;
                    if ( matcher.cache() )
                    {
                        view = new CachedStaticView( matcher, new ResponseMethod( m, viewDeclaration ) );
                    } else
                    {
                        view = new StaticView( matcher, new ResponseMethod( m, viewDeclaration ) );
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

}
