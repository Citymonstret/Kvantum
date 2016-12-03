/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
package com.plotsquared.iserver.internal;

import com.plotsquared.iserver.config.Message;
import com.plotsquared.iserver.core.CoreConfig;
import com.plotsquared.iserver.core.Worker;
import com.plotsquared.iserver.util.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SocketHandler
{

    private final ExecutorService executorService;
    private final List<SocketFilter> socketFilters;

    public SocketHandler()
    {
        this.executorService = Executors.newFixedThreadPool( CoreConfig.workers );
        this.socketFilters = new ArrayList<>();
        // TODO: Add config option to enable and disable socket filters
        // These are just temporary
        this.socketFilters.add( SOCKET_FILTER_IS_ACTIVE );
    }

    public void acceptSocket(final Socket s)
    {
        for ( final SocketFilter filter : socketFilters )
        {
            if ( !filter.filter( s ) )
            {
                Logger.debug( "SocketFilter filtered out socket: " + s ); // TODO: Remove
                breakSocketConnection( s );
                return;
            }
        }
        this.executorService.execute( () -> Worker.getAvailableWorker().run( s ) );
    }


    private void breakSocketConnection(final Socket s)
    {
        try
        {
            s.close();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    public void handleShutdown()
    {
        Message.WAITING_FOR_EXECUTOR_SERVICE.log();
        try
        {
            this.executorService.awaitTermination( 5, TimeUnit.SECONDS );
        } catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }


    public static final SocketFilter SOCKET_FILTER_IS_ACTIVE = socket -> !socket.isClosed() && socket.isConnected();

    public interface SocketFilter
    {

        boolean filter(final Socket socket);

    }

    public static Map<String, SocketFilter> getSocketFilters() throws Exception
    {
        final Map<String, SocketFilter> filters = new HashMap<>();
        for ( final Field field : SocketHandler.class.getDeclaredFields() )
        {
            if ( Modifier.isStatic( field.getModifiers() ) && field.getType().equals( SocketFilter.class ) )
            {
                filters.put( field.getName(), ( SocketFilter ) field.get( null ) );
            }
        }
        return filters;
    }

}
