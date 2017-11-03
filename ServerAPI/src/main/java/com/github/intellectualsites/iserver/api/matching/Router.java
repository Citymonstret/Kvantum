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
package com.github.intellectualsites.iserver.api.matching;

import com.github.intellectualsites.iserver.api.core.IntellectualServer;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.views.RequestHandler;
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
    public abstract RequestHandler match(Request request);

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
    public void dump(final IntellectualServer server)
    {
        throw new NotImplementedException( "Dump has not been overridden by the Router implementation" );
    }

}
