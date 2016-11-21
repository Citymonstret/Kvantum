package com.plotsquared.iserver.internal;

import com.plotsquared.iserver.config.Message;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.util.Assert;

import javax.net.ssl.SSLServerSocket;

public final class HTTPSThread extends Thread
{

    private final SocketHandler socketHandler;
    private final SSLServerSocket sslSocket;

    public HTTPSThread(final SSLServerSocket sslSocket, final SocketHandler socketHandler)
    {
        super( "SSL-Runner" );
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
