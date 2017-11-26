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

import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.views.ViewReturn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

final public class ResponseMethod implements BiConsumer<AbstractRequest, Response>, ViewReturn
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

    public Response handle(final AbstractRequest r)
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
    public void accept(AbstractRequest request, Response response)
    {
        response.copyFrom( handle( request ) );
    }

    @Override
    public final Response get(AbstractRequest r)
    {
        return this.handle( r );
    }
}
