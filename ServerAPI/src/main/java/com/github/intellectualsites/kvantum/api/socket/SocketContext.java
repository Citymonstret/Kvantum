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
package com.github.intellectualsites.kvantum.api.socket;

import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.util.ITempFileManager;
import lombok.Getter;

import javax.net.ssl.SSLSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Socket context used to make sure that sockets are handled
 * the same way, across implementations
 */
final public class SocketContext
{

    @Getter
    private final Socket socket;

    @Getter
    private final ITempFileManager tempFileManager;

    /**
     * Construct a new socket context from a socket
     *
     * @param socket Incoming socket
     */
    public SocketContext(final Socket socket)
    {
        this.socket = socket;
        this.tempFileManager = ServerImplementation.getImplementation().getTempFileManagerFactory()
                .newTempFileManager();
    }

    /**
     * Check if the socket is connected over SSL
     *
     * @return true if the socket is connected over SSL (is a {@link SSLSocket})
     */
    public boolean isSSL()
    {
        return this.socket instanceof SSLSocket;
    }

    /**
     * Close the socket and its resources
     */
    public void close()
    {
        if ( this.isActive() )
        {
            try
            {
                this.socket.close();
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }
        try
        {
            this.tempFileManager.clearTempFiles();
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Get the socket address
     *
     * @return socket address
     */
    public InetAddress getAddress()
    {
        return this.socket.getInetAddress();
    }

    /**
     * Check if the socket is active
     *
     * @return true if the socket ias not closed and is connected
     */
    public boolean isActive()
    {
        return this.socket != null &&
                !this.socket.isClosed() &&
                this.socket.isConnected();
    }

}
