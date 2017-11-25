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

import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;

/**
 * SSL implementation of the ordinary runner
 */
final class HTTPSThread extends Thread
{

    private final SocketHandler socketHandler;
    private final SSLServerSocket sslSocket;

    HTTPSThread(final SocketHandler socketHandler)
            throws KvantumInitializationException
    {
        super( "https" );
        this.setPriority( Thread.MAX_PRIORITY );
        this.socketHandler = Assert.notNull( socketHandler );
        try
        {
            final SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            this.sslSocket = Assert.notNull( (SSLServerSocket) factory.createServerSocket( CoreConfig.SSL.port ) );
        } catch ( IOException e )
        {
            throw new KvantumInitializationException( "Failed to create SSL socket", e );
        }
    }

    void close()
    {
        try
        {
            this.sslSocket.close();
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
                this.socketHandler.acceptSocket( new SocketContext( sslSocket.accept() ) );
            } catch ( final Exception e )
            {
                Message.TICK_ERROR.log();
                e.printStackTrace();
            }
        }
    }
}
