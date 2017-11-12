package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.Message;
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
                socketHandler.acceptSocket( serverSocket.accept() );
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
