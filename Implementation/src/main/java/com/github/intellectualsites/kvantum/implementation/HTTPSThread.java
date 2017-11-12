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
import com.github.intellectualsites.kvantum.api.util.Assert;

import javax.net.ssl.SSLServerSocket;

/**
 * SSL implementation of the ordinary runner
 */
final class HTTPSThread extends Thread
{

    private final SocketHandler socketHandler;
    private final SSLServerSocket sslSocket;

    HTTPSThread(final SSLServerSocket sslSocket, final SocketHandler socketHandler)
    {
        super( "https" );
        this.setPriority( Thread.MAX_PRIORITY );
        this.socketHandler = Assert.notNull( socketHandler );
        this.sslSocket = Assert.notNull( sslSocket );
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
                this.socketHandler.acceptSocket( sslSocket.accept() );
            } catch ( final Exception e )
            {
                Message.TICK_ERROR.log();
                e.printStackTrace();
            }
        }
    }
}
