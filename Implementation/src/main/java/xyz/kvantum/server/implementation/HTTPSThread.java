/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.ssl.SslHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import lombok.NonNull;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.ProtocolType;
import xyz.kvantum.server.implementation.error.KvantumInitializationException;

/**
 * SSL implementation of the ordinary runner
 */
final class HTTPSThread extends Thread
{

	//
	// Netty
	//
	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workerGroup;
	private final ServerBootstrap serverBootstrap;

	private ChannelFuture future;

	HTTPSThread(@NonNull final NioClassResolver classResolver) throws KvantumInitializationException
	{
		super( "https" );
		this.setPriority( Thread.MAX_PRIORITY );

		this.workerGroup = classResolver.getClassProvider()
				.getEventLoopGroup( CoreConfig.Pools.httpsWorkerGroupThreads );
		this.bossGroup = classResolver.getClassProvider().getEventLoopGroup( CoreConfig.Pools.httpsBossGroupThreads );

		try
		{
			final KeyStore keyStore = KeyStore.getInstance( "JKS" );
			keyStore.load( new FileInputStream( new File( CoreConfig.SSL.keyStore ) ),
					CoreConfig.SSL.keyStorePassword.toCharArray() );
			final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( "SunX509" );
			keyManagerFactory.init( keyStore, CoreConfig.SSL.keyStorePassword.toCharArray() );
			final SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( keyManagerFactory.getKeyManagers(), null, null );

			this.serverBootstrap = new ServerBootstrap();
			serverBootstrap.group( bossGroup, workerGroup )
					.channel( classResolver.getClassProvider().getServerSocketChannelClass() )
					.childHandler( new ChannelInitializer<SocketChannel>()
					{
						@Override protected void initChannel(final SocketChannel ch) throws Exception
						{
							final SSLEngine sslEngine = sslContext.createSSLEngine();
							sslEngine.setUseClientMode( false );
							sslEngine.setNeedClientAuth( false );
							ch.pipeline().addLast( new SslHandler( sslEngine ) );
							ch.pipeline().addLast( new KvantumReadTimeoutHandler() );
							ch.pipeline().addLast( new ByteArrayEncoder() );
							ch.pipeline().addLast( new KvantumServerHandler( ProtocolType.HTTPS ) );
						}
					} );
		} catch ( final NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | UnrecoverableKeyException | KeyManagementException e )
		{
			throw new KvantumInitializationException( "Failed to create SSL socket", e );
		}
	}

	void close()
	{
		try
		{
			if ( this.future != null )
			{
				Logger.info( "Closing ssl boss group..." );
				this.bossGroup.shutdownGracefully().sync();
				Logger.info( "Closing ssl worker group..." );
				this.workerGroup.shutdownGracefully().sync();
				Logger.info( "Closed ssl!" );
			}
		} catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}
	}

	@Override public void run()
	{
		try
		{
			this.future = serverBootstrap.bind( CoreConfig.SSL.port ).sync();
		} catch ( final InterruptedException e )
		{
			e.printStackTrace();
		}
	}
}
