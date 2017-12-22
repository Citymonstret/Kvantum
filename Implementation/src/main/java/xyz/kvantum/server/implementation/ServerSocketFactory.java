/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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

import lombok.Getter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.logging.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;

/**
 * Class responsible for finding appropriate ports
 */
class ServerSocketFactory
{

    @Getter
    private int serverSocketPort;

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
                ServerSocket serverSocket = new ServerSocket( port );
                serverSocket.close();
                bound = true;
            } catch ( final BindException e )
            {
                if ( e.getMessage().startsWith( "Permission denied" ) )
                {
                    Logger.error( "Failed to bind to privileged port, trying 1024 instead." );
                    port = 1024;
                } else if ( e.getMessage().startsWith( "Address already in use" ) )
                {
                    Logger.error( "Port {0} is occupied. Trying {1}...", port, ++port );
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

        this.serverSocketPort = port;
        return true;
    }

}
