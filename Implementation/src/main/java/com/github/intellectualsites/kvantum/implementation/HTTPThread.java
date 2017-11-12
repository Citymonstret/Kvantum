/*
 * Kvantum is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.socket.SocketContext;
import com.github.intellectualsites.kvantum.api.util.Assert;

import java.net.ServerSocket;

final class HTTPThread extends Thread
{

    private final ServerSocket serverSocket;
    private final SocketHandler socketHandler;

    HTTPThread(final ServerSocket serverSocket, final SocketHandler socketHandler)
    {
        super( "http" );
        this.setPriority( Thread.MAX_PRIORITY );
        this.serverSocket = Assert.notNull( serverSocket );
        this.socketHandler = Assert.notNull( socketHandler );
    }

    @Override
    public void run()
    {
        for ( ; ; )
        {
            if ( Server.getInstance().isStopping() )
            {
                break;
            }
            if ( Server.getInstance().isPaused() )
            {
                continue;
            }
            try
            {
                socketHandler.acceptSocket( new SocketContext( serverSocket.accept() ) );
            } catch ( final Exception e )
            {
                if ( !serverSocket.isClosed() )
                {
                    Server.getInstance().log( Message.TICK_ERROR );
                    e.printStackTrace();
                } else
                {
                    break;
                }
            }
        }
    }

}
