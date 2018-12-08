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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import java.net.SocketAddress;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.events.ConnectionEstablishedEvent;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.Request;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.ProtocolType;

/**
 * This is not split into multiple handlers because the netty implementation is supposed to interfere as little as
 * possible with the rest of the project. It's there as a necessity and thus it should remain as contained as possible.
 */
@RequiredArgsConstructor final class KvantumServerHandler extends ChannelInboundHandlerAdapter
{

	//
	// Cached content
	//
	static final String CONTENT_TYPE = "content_type";
	static final byte[] NEW_LINE = AsciiString.of( "\n" ).getValue();
	static final byte[] COLON_SPACE = AsciiString.of( ": " ).getValue();
	static final byte[] SPACE = AsciiString.of( " " ).getValue();
	static final byte[] CRLF = AsciiString.of( "\r\n" ).getValue();
	static final byte[] END_CHUNK = AsciiString.of( "0\r\n\r\n" ).getValue();
	static final AsciiString KEEP_ALIVE = AsciiString.of( "keep-alive" );
	static final AsciiString CLOSE = AsciiString.of( "close" );
	static final AsciiString CONNECTION = AsciiString.of( "connection" );
	static final int MAX_LENGTH = AsciiString.of( Integer.toHexString( Integer.MAX_VALUE ) ).length() + ( 2 * CRLF.length );

	//
	// Metrics
	//
	static final Timer TIMER_WRITE_RESPONSE = ServerImplementation.getImplementation().getMetrics().getRegistry()
			.timer( MetricRegistry.name( KvantumServerHandler.class, "writeResponse" ) );
	static final Timer TIMER_SEND_RESPONSE = ServerImplementation.getImplementation().getMetrics().getRegistry()
			.timer( MetricRegistry.name( KvantumServerHandler.class, "sendResponse" ) );
	static final Timer TIMER_READ_REQUEST = ServerImplementation.getImplementation().getMetrics().getRegistry()
			.timer( MetricRegistry.name( KvantumServerHandler.class, "readRequest" ) );
	static final Timer TIMER_TOTAL_SEND = ServerImplementation.getImplementation().getMetrics().getRegistry()
			.timer( MetricRegistry.name( KvantumServerHandler.class, "totalSend" ) );
	static final Timer TIMER_ROUTING = ServerImplementation.getImplementation().getMetrics().getRegistry()
			.timer( MetricRegistry.name( KvantumServerHandler.class, "routing" ) );
	static final Timer TIMER_WRITE_TO_CLIENT = ServerImplementation.getImplementation().getMetrics().getRegistry()
			.timer( MetricRegistry.name( KvantumServerHandler.class, "writeToClient" ) );
	static final Timer TIMER_READ_BYTES = ServerImplementation.getImplementation().getMetrics()
			.getRegistry().timer( MetricRegistry.name( KvantumServerHandler.class, "readBytes" ) );

	//
	// Instance variables
	//
	private final ProtocolType protocolType;

	private Timer.Context totalTimer;
	private WorkerContext workerContext;
	private RequestReader requestReader;
	private boolean reused = false;

	@Override public void handlerAdded(final ChannelHandlerContext context)
	{
		//
		// Prepare request
		//
		final Supplier<Boolean> activeCheck = () -> context.channel().isOpen() && context.channel().isActive();
		final SocketAddress remoteAddress = context.channel().remoteAddress();
		this.createNew( new SocketContext( this.protocolType, remoteAddress, activeCheck ) );

		//
		// Log "Connection accepted from '{}' - Handling the data!"
		//
		Message.CONNECTION_ACCEPTED.log( workerContext.getSocketContext().getAddress() );
	}

	private void createNew(@NonNull final SocketContext socketContext)
	{
		this.workerContext = new WorkerContext( ServerImplementation.getImplementation(),
				ServerImplementation.getImplementation().getProcedure().getInstance(), this );
		this.workerContext.setSocketContext( socketContext );
		final Request request = new Request( socketContext );
		this.workerContext.setRequest( request );
		this.requestReader = new RequestReader( request );
	}

	@Override public void channelActive(final ChannelHandlerContext ctx) throws Exception
	{
		//
		// It is pushed here, rather than in #handlerAdded, as it would otherwise
		// be impossible to gracefully shutdown the connection.
		// Blame the HTTP protocol for that.
		//
		final ConnectionEstablishedEvent connectionEstablishedEvent = new ConnectionEstablishedEvent(
				this.workerContext.getSocketContext().getIP() );
		ServerImplementation.getImplementation().getEventBus().throwEvent( connectionEstablishedEvent, false );
		//
		// Events may cancel the event, in which case we close the context
		//
		if ( connectionEstablishedEvent.isCancelled() )
		{
			ctx.close();
		}
		super.channelActive( ctx );
	}

	@Override public void channelRead(final ChannelHandlerContext context, final Object messageObject)
	{
		//
		// Debug message to indicate that the handler is reused
		//
		if ( reused && CoreConfig.debug )
		{
			Logger.debug( "Reused socket: {}", this.workerContext.getSocketContext().getIP() );
		}

		//
		// Always reset "reused" state
		//
		this.reused = false;

		//
		// Read incoming message
		//
		final ByteBuf message = ( ByteBuf ) messageObject;

		//
		// Create a new timer context for each request
		//
		if ( this.requestReader.isCleared() )
		{
			totalTimer = TIMER_READ_REQUEST.time();
		}

		//
		// Read all available data and try to compile it
		//
		Timer.Context readBytesTimer = TIMER_READ_BYTES.time();
		try
		{
			this.requestReader.readBytes( message );
		} catch ( final Throwable throwable )
		{
			new ResponseTask( context, this.workerContext )
					.handleThrowable( throwable, context );
		} finally
		{
			message.release();
			readBytesTimer.close();
		}

		//
		// Handle complete requests
		//
		if ( this.requestReader.isDone() )
		{
			//
			// Release content
			//
			this.requestReader.clear();
			this.workerContext.getRequest().onCompileFinish();

			if ( CoreConfig.debug )
			{
				this.workerContext.getRequest().dumpRequest();
			}

			//
			// Close the read timer
			//
			this.totalTimer.stop();

			this.handleResponse( context );

			if ( workerContext.getRequest().getHeaders().getOrDefault( KvantumServerHandler.CONNECTION, CLOSE )
					.equalsIgnoreCase( KvantumServerHandler.KEEP_ALIVE ) )
			{
				this.reused = true;
				this.requestReader.clear();
				this.createNew( workerContext.getSocketContext() );
			}
		}
	}
	
	@SuppressWarnings( "unused" )
	private void handleResponse(final ChannelHandlerContext context)
	{
		ServerImplementation.getImplementation().getExecutorService().execute(
						new ResponseTask( context, this.workerContext ) );
	}

	@Override public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause)
	{
		if ( cause instanceof ReadTimeoutException )
		{
			if ( CoreConfig.debug )
			{
				Logger.debug( "Connection for {} timed out", workerContext.getSocketContext().getIP() );
			}
		} else
		{
			Logger.error( "Encountered error..." );
			cause.printStackTrace();
		}
		context.close();
	}

}
