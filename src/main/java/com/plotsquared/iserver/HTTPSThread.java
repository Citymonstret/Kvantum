/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver;

import com.plotsquared.iserver.api.config.Message;
import com.plotsquared.iserver.api.util.Assert;
import com.plotsquared.iserver.core.Server;

import javax.net.ssl.SSLServerSocket;

public final class HTTPSThread extends Thread
{

    private final SimpleSocketHandler SocketHandler;
    private final SSLServerSocket sslSocket;

    public HTTPSThread(final SSLServerSocket sslSocket, final SimpleSocketHandler SocketHandler)
    {
        super( "SSL-Runner" );
        this.SocketHandler = Assert.notNull( SocketHandler );
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
                this.SocketHandler.acceptSocket( sslSocket.accept() );
            } catch ( final Exception e )
            {
                Message.TICK_ERROR.log();
                e.printStackTrace();
            }
        }
    }
}
