package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import lombok.Getter;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

/**
 * Class responsible for creating {@link ServerSocket} instances
 */
class ServerSocketFactory
{

    @Getter
    private ServerSocket serverSocket;

    /**
     * Attempt to create a new server socket
     *
     * @return true if the socket was created, false if not
     */
    boolean createServerSocket()
    {
        int port = CoreConfig.port;
        boolean bound = false;

        while ( !bound )
        {
            try
            {
                serverSocket = new ServerSocket( port );
                bound = true;
            } catch ( final BindException e )
            {
                if ( e.getMessage().startsWith( "Permission denied" ) )
                {
                    Logger.error( "Failed to bind to privileged port, trying 1024 instead." );
                    port = 1024;
                } else if ( e.getMessage().startsWith( "Address already in use" ) )
                {
                    Logger.error( "Port %s is occupied. Trying %s...", port, ++port );
                } else
                {
                    Logger.error( e.getMessage() );
                    return false;
                }
            } catch ( IOException e )
            {
                e.printStackTrace();
                return false;
            }
        }

        if ( port != CoreConfig.port )
        {
            Message.PORT_SWITCHED.log( CoreConfig.port, port );
            CoreConfig.port = port;
        }

        return true;
    }

}
