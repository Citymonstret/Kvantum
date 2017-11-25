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
package xyz.kvantum.server.api.socket;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.util.ITempFileManager;

import javax.net.ssl.SSLSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Socket context used to make sure that sockets are handled
 * the same way, across implementations
 */
@EqualsAndHashCode(of = "socketId")
@RequiredArgsConstructor
final public class SocketContext
{

    private static final AtomicLong socketIdPoll = new AtomicLong( Long.MIN_VALUE );

    @Getter
    final long socketId = socketIdPoll.getAndIncrement();

    @Getter
    private final Socket socket;

    private ITempFileManager tempFileManager;

    /**
     * Get a {@link ITempFileManager} for this socket instance.
     * This loaded lazily, so it will be created on the first call to
     * this method.
     *
     * @return ITempFileManager instance
     */
    public ITempFileManager getTempFileManager()
    {
        if ( tempFileManager == null )
        {
            this.tempFileManager = ServerImplementation.getImplementation().getTempFileManagerFactory()
                    .newTempFileManager();
        }
        return tempFileManager;
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
