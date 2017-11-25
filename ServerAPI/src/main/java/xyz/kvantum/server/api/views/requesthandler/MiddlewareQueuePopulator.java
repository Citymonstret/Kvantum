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
package xyz.kvantum.server.api.views.requesthandler;

import java.util.ArrayList;
import java.util.Collection;

public final class MiddlewareQueuePopulator
{

    private final Collection<Class<? extends Middleware>> middlewares = new ArrayList<>();

    public void add(final Class<? extends Middleware> middleware)
    {
        if ( Middleware.class == middleware )
        {
            return;
        }
        try
        {
            middleware.getConstructor();
        } catch ( final Exception e )
        {
            e.printStackTrace();
            return;
        }
        this.middlewares.add( middleware );
    }

    public MiddlewareQueue generateQueue()
    {
        final MiddlewareQueue queue = new MiddlewareQueue();
        middlewares.forEach( clazz ->
        {
            try
            {
                queue.add( clazz.newInstance() );
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        } );
        return queue;
    }

}
