//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.util;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.views.RequestHandler;
import com.plotsquared.iserver.views.errors.View404;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class RequestManager
{

    private final List<RequestHandler> views;

    public RequestManager()
    {
        this.views = new ArrayList<>();
    }

    public void add(final RequestHandler view)
    {
        Assert.notNull( view );

        final Optional<RequestHandler> illegalRequestHandler = LambdaUtil.getFirst( views, v -> v.toString().equalsIgnoreCase( view.toString() ) );
        if ( illegalRequestHandler.isPresent() )
        {
            throw new IllegalArgumentException( "Duplicate view pattern!" );
        }
        views.add( view );
    }

    public RequestHandler match(final Request request)
    {
        Assert.notNull( request );

        final Optional<RequestHandler> view = LambdaUtil.getFirst( views, request.matches );
        if ( view.isPresent() )
        {
            return view.get();
        }
        return View404.construct( request.getQuery().getFullRequest() );
    }

    public void dump(final Server server)
    {
        Assert.notNull( server );

        ( (IConsumer<RequestHandler>) view -> server.log( "> RequestHandler - Class '%s', Regex: '%s'",
                view.getClass().getSimpleName(), view.toString() ) ).foreach( views );
    }

    public void remove(final RequestHandler view)
    {
        Assert.notNull( view );

        if ( views.contains( view ) )
        {
            views.remove( view );
        }
    }

    public void clear()
    {
        this.views.clear();
        Server.getInstance().log( "Cleared views." );
    }

}
