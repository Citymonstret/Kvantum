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
package com.github.intellectualsites.iserver.api.views.requesthandler;


import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.util.Assert;

import java.util.ArrayDeque;
import java.util.Queue;

public final class MiddlewareQueue
{

    private final Queue<Middleware> queue = new ArrayDeque<>();

    private boolean finished = false;

    public void add(final Middleware middleware)
    {
        this.queue.add( middleware );
    }

    public void handle(final Request request)
    {
        Assert.isValid( request );

        final Middleware next = this.queue.poll();
        if ( next != null )
        {
            next.handle( request, this );
        } else
        {
            finished = true;
        }
    }

    public final boolean finished()
    {
        return finished;
    }

}
