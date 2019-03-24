/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import lombok.NonNull;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.ProtocolType;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;

@SuppressWarnings({"WeakerAccess", "unused"}) final class HTTPThread extends Thread {

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

    HTTPThread(@NonNull final ServerSocketFactory serverSocketFactory,
        @NonNull final NioClassResolver classResolver) throws KvantumInitializationException {
        super("http");
        this.setPriority(Thread.MAX_PRIORITY);

        this.workerGroup = classResolver.getClassProvider()
            .getEventLoopGroup(CoreConfig.Pools.httpWorkerGroupThreads);
        this.bossGroup = classResolver.getClassProvider()
            .getEventLoopGroup(CoreConfig.Pools.httpBossGroupThreads);

        if (!serverSocketFactory.createServerSocket()) {
            throw new KvantumInitializationException("Failed to start server...");
        }

        this.port = serverSocketFactory.getServerSocketPort();

        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.group(bossGroup, workerGroup)
            .channel(classResolver.getClassProvider().getServerSocketChannelClass())
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override protected void initChannel(final SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new KvantumReadTimeoutHandler())
                        .addLast(new ByteArrayEncoder())
                        .addLast(new KvantumServerHandler(ProtocolType.HTTP));
                }
            });
    }

    void close() {
        try {
            if (this.future != null) {
                Logger.info("Closing boss group...");
                this.bossGroup.shutdownGracefully().sync();
                Logger.info("Closing worker group...");
                this.workerGroup.shutdownGracefully().sync();
                Logger.info("Closed!");
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override public void run() {
        try {
            this.future = serverBootstrap.bind(this.port).sync();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

}
