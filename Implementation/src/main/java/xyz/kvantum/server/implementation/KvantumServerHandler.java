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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.cache.CacheApplicable;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.core.WorkerProcedure;
import xyz.kvantum.server.api.events.ConnectionEstablishedEvent;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.Request;
import xyz.kvantum.server.api.response.FinalizedResponse;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.HeaderOption;
import xyz.kvantum.server.api.response.KnownLengthStream;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.response.ResponseBody;
import xyz.kvantum.server.api.response.ResponseStream;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.DebugTree;
import xyz.kvantum.server.api.util.ProtocolType;
import xyz.kvantum.server.api.util.TimeUtil;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.errors.ViewException;
import xyz.kvantum.server.api.views.requesthandler.HTTPSRedirectHandler;
import xyz.kvantum.server.implementation.error.KvantumException;

/**
 * This is not split into multiple handlers because the netty implementation is supposed to interfere as little as
 * possible with the rest of the project. It's there as a necessity and thus it should remain as contained as possible.
 */
@RequiredArgsConstructor final class KvantumServerHandler extends ChannelInboundHandlerAdapter
{

	private static final String CONTENT_TYPE = "content_type";
	private static final byte[] NEW_LINE = AsciiString.of( "\n" ).getValue();
	private static final byte[] COLON_SPACE = AsciiString.of( ": " ).getValue();
	private static final byte[] SPACE = AsciiString.of( " " ).getValue();
	private static final byte[] CRLF = AsciiString.of( "\r\n" ).getValue();
	private static final byte[] END_CHUNK = AsciiString.of( "0\r\n" ).getValue();
	private static final AsciiString KEEP_ALIVE = AsciiString.of( "keep-alive" );
	private static final AsciiString CLOSE = AsciiString.of( "close" );
	private static final AsciiString CONNECTION = AsciiString.of( "connection" );

	private final ProtocolType protocolType;

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
		ServerImplementation.getImplementation().getEventBus().emit( connectionEstablishedEvent );
		if ( connectionEstablishedEvent.isCancelled() )
		{
			ctx.close();
		}
		super.channelActive( ctx );
	}

	@Override public void channelRead(final ChannelHandlerContext context, final Object messageObject)
	{
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

		try
		{
			this.requestReader.readBytes( message );
		} catch ( final Throwable throwable )
		{
			this.handleThrowable( throwable, context );
		} finally
		{
			message.release();
		}

		//
		// Handle complete requests
		//
		if ( this.requestReader.isDone() )
		{
			// Release content
			this.requestReader.clear();
			this.workerContext.getRequest().onCompileFinish();

			if ( CoreConfig.debug )
			{
				this.workerContext.getRequest().dumpRequest();
			}

			try
			{
				this.handleResponse( context );
			} catch ( final Throwable throwable )
			{
				this.handleThrowable( throwable, context );
			}
		}
	}

	private void handleThrowable(final Throwable throwable, final ChannelHandlerContext context)
	{
		if ( throwable instanceof ReturnStatus )
		{
			final ReturnStatus returnStatus = ( ReturnStatus ) throwable;
			if ( returnStatus.getApplicableContext() == null )
			{
				returnStatus.setApplicableContext( this.workerContext );
			}

			final Response response = new Response();
			response.getHeader().clear();
			response.getHeader().setStatus( returnStatus.getStatus() );
			response.getHeader().set( Header.HEADER_CONNECTION, CLOSE );
			this.workerContext.setBody( response );

			this.sendResponse( context );
		} else
		{
			new KvantumException( "Failed to handle incoming socket", throwable ).printStackTrace();
		}
	}

	private void writeResponse() throws Throwable
	{
		//
		// Scope variables
		//
		RequestHandler requestHandler = workerContext.getRequestHandler();
		AbstractRequest request = workerContext.getRequest();
		ResponseBody body;
		ResponseStream responseStream = null;
		boolean cache = false, shouldCache = false;

		try
		{
			//
			// Validate the request, if there are
			// registered request validators
			//
			if ( !requestHandler.getValidationManager().isEmpty() )
			{
				requestHandler.getValidationManager().validate( request );
			}

			if ( requestHandler instanceof CacheApplicable &&
					( ( CacheApplicable ) requestHandler ).isApplicable( request ) )
			{
				cache = true;
				if ( !ServerImplementation.getImplementation().getCacheManager().hasCache( requestHandler ) )
				{
					shouldCache = true;
				}
			}

			//
			// Make sure that cache is handled as it should
			//
			if ( !cache || shouldCache )
			{ // Either it's a non-cached view, or there is no cache stored
				body = requestHandler.handle( request );
				if ( CoreConfig.debug )
				{
					Logger.debug( "Did not find cache for request handler: {}", requestHandler.getName() );
				}
			} else
			{
				// Just read from memory
				body = ServerImplementation.getImplementation().getCacheManager().getCache( requestHandler );
				if ( CoreConfig.debug )
				{
					Logger.debug( "Found request handler in cache: {}", requestHandler.getName() );
				}
			}

			//
			// If the body is null, it is either marked for an internal redirect
			// or something went wrong. In any case, abort.
			//
			if ( body == null )
			{
				final Object redirect = request.getMeta( "internalRedirect" );
				if ( redirect != null && redirect instanceof AbstractRequest )
				{
					if ( CoreConfig.debug )
					{
						Logger.debug( "Found internal redirect..." );
					}
					final AbstractRequest redirectRequest = ( AbstractRequest ) redirect;
					redirectRequest.removeMeta( "internalRedirect" );
					workerContext.setRequest( redirectRequest );
					if ( CoreConfig.debug )
					{
						Logger.debug( "Redirect is to " + redirectRequest.getQuery().getResource() );
					}
					this.determineRequestHandler();
					this.writeResponse();
				}
				return;
			}

			responseStream = body.getResponseStream();

			//
			// Store cache
			//
			if ( shouldCache && body.getResponseStream() instanceof KnownLengthStream )
			{
				ServerImplementation.getImplementation().getCacheManager().setCache( requestHandler, body );
			}

			//
			// Post-generation procedures
			//
			request.postponedCookies.forEach( body.getHeader()::setCookie );

			//
			// Add the content type meta to the request, can then be used by worker processes
			//
			final Optional<AsciiString> contentType = body.getHeader().get( Header.HEADER_CONTENT_TYPE );
			if ( contentType.isPresent() )
			{
				request.addMeta( CONTENT_TYPE, contentType.get().toString() );
			} else
			{
				request.addMeta( CONTENT_TYPE, null );
			}

			//
			// Allow text handlers to act upon the content, if the content is text and the
			// content length is known (can't act on stream)
			//
			if ( 	request.getQuery().getMethod().hasBody() &&
					body.isText() && responseStream instanceof KnownLengthStream &&
					workerContext.getWorkerProcedureInstance().containsHandlers() )
			{
				// If it's a string we have to read the entire string into memory, act on it, and return it
				final KnownLengthStream knownLengthStream = (KnownLengthStream) responseStream;

				String text = new String( knownLengthStream.getAll(), StandardCharsets.UTF_8 );
				for ( final WorkerProcedure.Handler<String> handler : workerContext.getWorkerProcedureInstance()
						.getStringHandlers() )
				{
					text = handler.act( requestHandler, request, text );
				}

				knownLengthStream.replaceBytes( text.getBytes( StandardCharsets.UTF_8 ) );
			}
		} catch ( final Exception e )
		{
			Message.WORKER_FAILED_HANDLING.log( e.getMessage() );

			if ( CoreConfig.verbose )
			{
				e.printStackTrace();
			}

			if ( CoreConfig.debug )
			{
				body = new ViewException( e ).generate( request );
			} else
			{
				body = null;
			}
		}

		if ( body == null || responseStream == null )
		{
			throw new ReturnStatus( Header.STATUS_INTERNAL_ERROR, workerContext );
		}

		workerContext.setBody( body );
		workerContext.setResponseStream( responseStream );
	}

	private void determineRequestHandler() throws Throwable
	{
		workerContext.setRequestHandler(
				ServerImplementation.getImplementation().getRouter().match( workerContext.getRequest() ) );
		if ( workerContext.getRequestHandler() == null )
		{
			throw new ReturnStatus( Header.STATUS_NOT_FOUND, workerContext );
		}
		if ( workerContext.getRequest().getProtocolType() != ProtocolType.HTTPS && workerContext.getRequestHandler()
				.forceHTTPS() )
		{
			if ( CoreConfig.debug )
			{
				Logger.debug( "Redirecting request [{}] to HTTPS version of [{}]", workerContext.getRequest(),
						workerContext.getRequestHandler() );
			}
			if ( !CoreConfig.SSL.enable )
			{
				Logger.error( "RequestHandler ({}) forces HTTPS but SSL runner not enabled!" );
				throw new ReturnStatus( Header.STATUS_INTERNAL_ERROR, workerContext );
			}
			workerContext.setRequestHandler( HTTPSRedirectHandler.getInstance() );
		}
	}

	private void handleResponse(final ChannelHandlerContext context) throws Throwable
	{
		this.determineRequestHandler();
		this.writeResponse();
		this.sendResponse( context );
	}

	@SuppressWarnings("ALL") private void sendResponse(final ChannelHandlerContext context)
	{
		//
		// Determine whether or not the responce should be compressed
		//
		workerContext.determineGzipStatus();

		//
		// Get the generated body
		//
		ResponseBody body = workerContext.getBody();

		// Make sure that the generated response is valid (not null)
		// TODO: Add more checks
		Assert.notNull( body );
		Assert.notNull( body.getHeader() );

		// Retrieve an Md5Handler from the handler pool
		// final Md5Handler md5Handler = SimpleServer.md5HandlerPool.getNullable();
		// Generate the md5 checksum
		// TODO: Re-enable this final String checksum = md5Handler.generateChecksum( bytes );
		// Update the headers to include the md5 checksum
		// body.getHeader().set( Header.HEADER_CONTENT_MD5, checksum );
		// body.getHeader().set( Header.HEADER_ETAG, checksum );
		// Return the md5 handler to the pool
		// SimpleServer.md5HandlerPool.add( md5Handler );

		//
		// Add a Last-Modified if it isn't already present in the response
		//
		if ( !body.getHeader().get( Header.HEADER_LAST_MODIFIED ).isPresent() )
		{
			body.getHeader().set( Header.HEADER_LAST_MODIFIED, TimeUtil.getHTTPTimeStamp() );
		}

		//
		// Output debug messages
		//
		if ( CoreConfig.debug )
		{
			DebugTree.builder().name( "Response Information" )
					.entry( "Address", workerContext.getSocketContext().getAddress() )
					.entry( "Headers", body.getHeader().getHeaders() ).build().collect().forEach( Logger::debug );
		}

		//
		// Get the respone stream
		//
		final ResponseStream responseStream = workerContext.getResponseStream(); // body.getResponseStream();
		final boolean hasKnownLength = responseStream instanceof KnownLengthStream;

		//
		// Fetch the GZIP handler, if applicable
		//
		final GzipHandler gzipHandler;
		if ( workerContext.isGzip() )
		{
			gzipHandler = SimpleServer.gzipHandlerPool.getNullable();
		} else
		{
			gzipHandler = null;
		}

		//
		// Send either the transfer encoding or content length, important for keep-alive
		//
		if ( hasKnownLength )
		{
			//
			// If the length is known, we compress before writing
			//
			if ( workerContext.isGzip() )
			{
				byte[] bytes = ( ( KnownLengthStream ) responseStream ).getAll();
				try
				{
					bytes = gzipHandler.compress( bytes );
				} catch ( final IOException e )
				{
					new KvantumException( "( GZIP ) Failed to compress the bytes" ).printStackTrace();
				}
				( ( KnownLengthStream) responseStream ).replaceBytes( bytes );
			}
			body.getHeader().set( Header.HEADER_CONTENT_LENGTH, AsciiString.of( ( ( KnownLengthStream) responseStream ).getLength() ) );
		} else
		{
			body.getHeader().set( Header.HEADER_TRANSFER_ENCODING, "chunked" );
		}

		//
		// Determine whether to keep the connection alive
		//
		final boolean keepAlive;
		if ( workerContext.getRequest().getHeaders().getOrDefault( CONNECTION, CLOSE ).equalsIgnoreCase( KEEP_ALIVE ) )
		{
			if ( CoreConfig.debug )
			{
				Logger.debug( "Request " + workerContext.getRequest() + " requested keep-alive..." );
			}
			keepAlive = true;
			//
			// Apply "connection: keep-alive" and "content-length: n" headers to
			// make sure that the client keeps the connection open
			//
			body.getHeader().set( Header.HEADER_CONNECTION, KEEP_ALIVE );
		} else
		{
			keepAlive = false;
			body.getHeader().set( Header.HEADER_CONNECTION, CLOSE );
		}

		//
		// Alocate a byte buffer
		//
		final ByteBuf buf = context.alloc().buffer( CoreConfig.Buffer.out );

		//
		// Write the header
		//
		buf.writeBytes( body.getHeader().getFormat().getValue() );
		buf.writeBytes( SPACE );
		buf.writeBytes( body.getHeader().getStatus().getValue() );
		buf.writeBytes( NEW_LINE );
		for ( final Map.Entry<HeaderOption, AsciiString> entry : body.getHeader().getHeaders().entries() )
		{
			buf.writeBytes( entry.getKey().getBytes() );
			buf.writeBytes( COLON_SPACE );
			buf.writeBytes( entry.getValue().getValue() );
			buf.writeBytes( NEW_LINE );
		}
		// Print one empty line to indicate that the header sending is finished, this is important as the content
		// would otherwise be classed as headers, which really isn't optimal <3
		buf.writeBytes( NEW_LINE );

		if ( CoreConfig.debug )
		{
			Logger.debug( "Using direct write from memory: {}", hasKnownLength );
		}

		//
		// Write the response
		//
		while ( !responseStream.isFinished() )
		{
			//
			// Read as much data as possible from the respone stream
			//
			byte[] bytes = responseStream.read( CoreConfig.Buffer.out );
			if ( bytes != null && bytes.length > 0 )
			{
				//
				// If the length is known, write data directly
				//
				if ( hasKnownLength )
				{
					buf.writeBytes( bytes );
				} else
				{
					//
					// If the length isn't known, we first compress (if applicable) and then write using
					// the chunked transfer encoding format
					//
					if ( workerContext.isGzip() )
					{
						try
						{
							bytes = gzipHandler.compress( bytes );
						} catch ( final IOException e )
						{
							new KvantumException( "( GZIP ) Failed to compress the bytes" ).printStackTrace();
							continue;
						}
					}
					// <size>
					buf.writeBytes( AsciiString.of( Integer.toHexString( bytes.length ) ).getValue() );
					// <crlf>
					buf.writeBytes( CRLF );
					// <chunk data>
					buf.writeBytes( bytes );
					// <crlf>
					buf.writeBytes( CRLF );
				}
			}
		}

		//
		// If we're using the chunked encoding format
		// write the end chunk
		//
		if ( !hasKnownLength )
		{
			// 0 \r\n
			// \r\n
			buf.writeBytes( END_CHUNK );
			buf.writeBytes( CRLF );
		}

		//
		// Return the GZIP handler to the pool
		//
		if ( gzipHandler != null )
		{
			SimpleServer.gzipHandlerPool.add( gzipHandler );
		}

		//
		// Invalidate request to make sure that it isn't handled anywhere else, again (wouldn't work)
		//
		workerContext.getRequest().setValid( false );

		//
		// Intialize a finalized response builder (used for logging)
		//
		final FinalizedResponse.FinalizedResponseBuilder finalizedResponse = FinalizedResponse.builder();

		//
		// Safety measure taken to make sure that IPs are not logged
		// in production mode. This is is to ensure GDPR compliance
		//
		if ( CoreConfig.debug )
		{
			finalizedResponse.address( this.workerContext.getSocketContext().getIP() );
		} else
		{
			finalizedResponse.address( "external" );
		}
		finalizedResponse.authorization( this.workerContext.getRequest().getAuthorization().orElse( null ) )
				/* .length( (int) actualLength ) */.status( body.getHeader().getStatus().toString() )
				.query( this.workerContext.getRequest().getQuery() ).timeFinished( System.currentTimeMillis() ).build();
		ServerImplementation.getImplementation().getEventBus().emit( finalizedResponse );

		//
		// Make sure everything is written and either close the connection
		// or the channel (depending on whether keep-alive is used or not)
		//
		final ChannelFuture future = context.writeAndFlush( buf );
		if ( keepAlive )
		{
			this.reused = true;
			this.requestReader.clear();
			this.createNew( workerContext.getSocketContext() );
		} else
		{
			future.addListener( ChannelFutureListener.CLOSE );
		}
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
