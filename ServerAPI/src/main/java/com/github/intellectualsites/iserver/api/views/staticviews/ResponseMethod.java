/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver.api.views.staticviews;

import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Response;
import com.github.intellectualsites.iserver.api.util.Assert;

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
