/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.views.staticviews;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.IConsumer;
import com.plotsquared.iserver.util.ReflectionUtils;
import com.plotsquared.iserver.views.RequestHandler;
import com.plotsquared.iserver.views.requesthandler.Middleware;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

final public class StaticViewManager
{

    private static final Class<?>[] parameters = new Class<?>[]{ Request.class };
    private static final Class<?>[] alternativeParameters = new Class<?>[]{ Request.class, Response.class };

    public static void generate(final Object viewDeclaration) throws Exception
    {
        final Class<?> clazz = viewDeclaration.getClass();
        final List<ReflectionUtils.AnnotatedMethod> annotatedMethods = ReflectionUtils.getAnnotatedMethods( ViewMatcher.class, clazz );
        ( (IConsumer<ReflectionUtils.AnnotatedMethod>) annotatedMethod ->
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
                    final ViewMatcher matcher = (ViewMatcher) annotatedMethod.getAnnotation();
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

                    com.plotsquared.iserver.core.ServerImplementation.getImplementation().getRouter().add( view );
                }
            }
        } ).foreach( annotatedMethods );
    }

}
