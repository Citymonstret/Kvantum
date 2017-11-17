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
package com.github.intellectualsites.kvantum.api.matching;

import com.github.intellectualsites.kvantum.api.core.Kvantum;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Router that is responsible for {@link RequestHandler} matching
 */
@SuppressWarnings("unused")
public abstract class Router
{

    /**
     * Attempt to match a request to a {@link RequestHandler}
     *
     * @param request Request to be matched
     * @return Depends on implementation, but should return either the matched
     * {@link RequestHandler} or null, may also return a Status 404 View.
     */
    public abstract RequestHandler match(AbstractRequest request);

    /**
     * Add a new {@link RequestHandler} to the router
     *
     * @param handler RequestHandler that is to be registered
     * @return The added {@link RequestHandler}
     */
    public abstract RequestHandler add(RequestHandler handler);

    /**
     * Attempts to remove a RequestHandler from the Router
     * @param handler RequestHandler to be removed
     */
    public abstract void remove(RequestHandler handler);

    /**
     * Clear all handlers from the router
     */
    public abstract void clear();

    /**
     * Dump Router contents onto the server log
     * @param server Server instance
     */
    public void dump(final Kvantum server)
    {
        throw new NotImplementedException( "Dump has not been overridden by the Router implementation" );
    }

}
