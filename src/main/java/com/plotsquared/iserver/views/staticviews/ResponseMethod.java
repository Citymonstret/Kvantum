package com.plotsquared.iserver.views.staticviews;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

final public class ResponseMethod implements BiConsumer<Request, Response>
{

    private final Method method;
    private final Object instance;
    private final boolean passResponse;

    public ResponseMethod(final Method method, final Object instance)
    {
        Assert.notNull( method, instance );

        this.method = method;
        this.method.setAccessible( true );
        this.instance = instance;
        this.passResponse = method.getReturnType() == Void.TYPE;
    }

    public Response handle(final Request r)
    {
        Assert.notNull( r );

        try
        {
            if ( passResponse )
            {
                final Response response = new Response();
                this.method.invoke( instance, r, response );
                return response;
            }
            return (Response) this.method.invoke( instance, r );
        } catch ( IllegalAccessException | InvocationTargetException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void accept(Request request, Response response)
    {
        response.copyFrom( handle( request ) );
    }
}
