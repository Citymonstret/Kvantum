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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.ProtocolType;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;

@SuppressWarnings({ "WeakerAccess", "unused" })
final class HTTPThread extends Thread
{

    //
    // Netty
    //
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ServerBootstrap serverBootstrap;
    //
    // Kvantum
    //
    private final int port;
    private ChannelFuture future;

    HTTPThread(final ServerSocketFactory serverSocketFactory)
            throws KvantumInitializationException
    {
        super( "http" );
        this.setPriority( Thread.MAX_PRIORITY );

        this.workerGroup = new NioEventLoopGroup( CoreConfig.Pools.httpWorkerGroupThreads );
        this.bossGroup = new NioEventLoopGroup( CoreConfig.Pools.httpBossGroupThreads );

        if ( !serverSocketFactory.createServerSocket() )
        {
            throw new KvantumInitializationException( "Failed to start server..." );
        }

        this.port = serverSocketFactory.getServerSocketPort();

        this.serverBootstrap = new ServerBootstrap();
        serverBootstrap.group( bossGroup, workerGroup )
                .channel( NioServerSocketChannel.class )
                .childHandler( new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(final SocketChannel ch) throws Exception
                    {
                        ch.pipeline().addLast( new KvantumServerHandler( ProtocolType.HTTP ) );
                    }
                } );
    }

    void close()
    {
        try
        {
            if ( this.future != null )
            {
                Logger.info( "Closing boss group..." );
                this.bossGroup.shutdownGracefully().sync();
                Logger.info( "Closing worker group..." );
                this.workerGroup.shutdownGracefully().sync();
                Logger.info( "Closed!" );
            }
        } catch ( final InterruptedException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try
        {
            this.future = serverBootstrap.bind( this.port ).sync();
        } catch ( final InterruptedException e )
        {
            e.printStackTrace();
        }
    }

}
