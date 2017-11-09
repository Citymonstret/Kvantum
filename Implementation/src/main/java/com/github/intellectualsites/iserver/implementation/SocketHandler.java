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
package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.config.ConfigurationFile;
import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.config.YamlConfiguration;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.logging.Logger;
import com.github.intellectualsites.iserver.api.socket.ISocketHandler;
import com.github.intellectualsites.iserver.api.socket.SocketFilter;
import com.github.intellectualsites.iserver.implementation.error.IntellectualServerException;

import java.io.File;
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

@SuppressWarnings({ "unused", "WeakerAccess" })
final class SocketHandler implements ISocketHandler
{

    @SuppressWarnings("ALL")
    public static final SocketFilter SOCKET_FILTER_IS_ACTIVE = Socket -> !Socket.isClosed() && Socket.isConnected();
    public static final SocketFilter SOCKET_FILTER_ENABLE_SOCKET = Socket -> false;

    private static final Map<String, SocketFilter> availableSocketFilters = new HashMap<>();

    static
    {
        availableSocketFilters.put( "1isActive", SOCKET_FILTER_IS_ACTIVE );
        availableSocketFilters.put( "0all", SOCKET_FILTER_ENABLE_SOCKET );
    }

    private final ExecutorService executorService;
    private final List<SocketFilter> socketFilters;

    SocketHandler()
    {
        this.executorService = Executors.newFixedThreadPool( CoreConfig.workers );
        this.socketFilters = new ArrayList<>();

        try
        {
            ConfigurationFile configurationFile = new YamlConfiguration( "socketFilters", new File( new File(
                    ServerImplementation
                            .getImplementation().getCoreFolder(), "config" ), "socketFilters.yml" ) );
            configurationFile.loadFile();
            availableSocketFilters.forEach( (key, value) -> configurationFile.setIfNotExists( key.substring( 1 ),
                    key.charAt( 0 ) == '1' ) );
            configurationFile.saveFile();
            availableSocketFilters.entrySet().stream().filter( entry -> configurationFile.get( entry.getKey()
                    .substring( 1 ) ) )
                    .forEach( entry -> socketFilters.add( entry.getValue() ) );
        } catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    static Map<String, SocketFilter> getSocketFilters() throws Exception
    {
        final Map<String, SocketFilter> filters = new HashMap<>();
        for ( final Field field : SocketHandler.class.getDeclaredFields() )
        {
            if ( Modifier.isStatic( field.getModifiers() ) && field.getType().equals( SocketFilter.class ) )
            {
                filters.put( field.getName(), (SocketFilter) field.get( null ) );
            }
        }
        return filters;
    }

    @Override
    public void acceptSocket(final Socket s)
    {
        for ( final SocketFilter filter : socketFilters )
        {
            if ( !filter.filter( s ) )
            {
                Logger.debug( "SocketFilter filtered out Socket: " + s );
                breakSocketConnection( s );
                return;
            }
        }
        if ( CoreConfig.debug )
        {
            Logger.debug( "Accepting Socket: " + s.toString() );
        }
        this.executorService.execute( () -> {
            try
            {
                WorkerPool.getAvailableWorker().run( s );
            } catch ( InterruptedException e )
            {
                new IntellectualServerException( "Failed to retrieve worker", e ).printStackTrace();
            }
        } );
    }

    @Override
    public void breakSocketConnection(final Socket s)
    {
        try
        {
            s.close();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void handleShutdown()
    {
        Message.WAITING_FOR_EXECUTOR_SERVICE.log();
        try
        {
            this.executorService.shutdownNow();
            this.executorService.awaitTermination( 10, TimeUnit.SECONDS );
        } catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }

}
