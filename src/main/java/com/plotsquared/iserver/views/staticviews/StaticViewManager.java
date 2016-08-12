package com.plotsquared.iserver.views.staticviews;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.IConsumer;
import com.plotsquared.iserver.util.ReflectionUtils;
import com.plotsquared.iserver.views.RequestHandler;
import com.plotsquared.iserver.views.decl.ResponseMethod;
import com.plotsquared.iserver.views.decl.ViewMatcher;
import com.plotsquared.iserver.views.requesthandler.Middleware;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

final public class StaticViewManager
{

    private static final Class<?>[] parameters = new Class<?>[]{ Request.class };
    private static final Class<?>[] alternativeParameters = new Class<?>[] { Request.class, Response.class };

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

                    Server.getInstance().getRequestManager().add( view );
                }
            }
        } ).foreach( annotatedMethods );
    }

}
