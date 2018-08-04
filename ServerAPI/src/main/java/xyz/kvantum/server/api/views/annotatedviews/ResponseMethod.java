/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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

import com.hervian.lambda.Lambda;
import com.hervian.lambda.LambdaFactory;
import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.NonNull;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.views.ViewReturn;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

final public class ResponseMethod<T, C> implements
        BiConsumer<AbstractRequest, Response>, ViewReturn
{

    private final Lambda lambda;
    private final C instance;
    private final boolean passResponse;
    private final OutputConverter outputConverter;

    ResponseMethod(@NonNull final Method method, @NonNull final C instance,
                   @Nullable final OutputConverter outputConverter) throws Throwable
    {
        Assert.notNull( method, instance );

        this.lambda = LambdaFactory.create( method );
        this.instance = instance;
        this.passResponse = method.getReturnType() == Void.TYPE;
        this.outputConverter = outputConverter;
    }

    public Response handle(final AbstractRequest r)
    {
        Assert.notNull( r );

        if ( passResponse )
        {
            final Response response = new Response();
            this.lambda.invoke_for_void( instance, r, response );
            return response;
        }
        final Object output = this.lambda.invoke_for_Object( instance, r );
        if ( outputConverter != null )
        {
            return outputConverter.generateResponse( output );
        }
        return (Response) output;
    }

    @Override
    public void accept(final AbstractRequest request, final Response response)
    {
        response.copyFrom( handle( request ) );
    }

    @Override
    public final Response get(final AbstractRequest r)
    {
        return this.handle( r );
    }
}
