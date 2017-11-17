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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.socket.SocketContext;
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
                this.socketHandler.acceptSocket( new SocketContext( sslSocket.accept() ) );
            } catch ( final Exception e )
            {
                Message.TICK_ERROR.log();
                e.printStackTrace();
            }
        }
    }
}
