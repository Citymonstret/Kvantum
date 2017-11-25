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
package xyz.kvantum.server.implementation;

import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;

import java.io.IOException;
import java.net.ServerSocket;

final class HTTPThread extends Thread
{

    private final ServerSocket serverSocket;
    private final SocketHandler socketHandler;

    HTTPThread(final ServerSocketFactory serverSocketFactory, final SocketHandler socketHandler)
            throws KvantumInitializationException
    {
        super( "http" );
        this.setPriority( Thread.MAX_PRIORITY );

        if ( !serverSocketFactory.createServerSocket() )
        {
            throw new KvantumInitializationException( "Failed to start server..." );
        }

        this.serverSocket = Assert.notNull( serverSocketFactory.getServerSocket() );
        this.socketHandler = Assert.notNull( socketHandler );
    }

    void close()
    {
        try
        {
            this.serverSocket.close();
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
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
