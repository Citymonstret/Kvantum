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

import static xyz.kvantum.server.implementation.KvantumServerHandler.CLOSE;
import static xyz.kvantum.server.implementation.KvantumServerHandler.KEEP_ALIVE;
import static xyz.kvantum.server.implementation.KvantumServerHandler.MAX_LENGTH;

import com.codahale.metrics.Timer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.cache.CacheApplicable;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.CoreConfig.Buffer;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.core.WorkerProcedure;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.FinalizedResponse;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.HeaderOption;
import xyz.kvantum.server.api.response.KnownLengthStream;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.response.ResponseBody;
import xyz.kvantum.server.api.response.ResponseStream;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.DebugTree;
import xyz.kvantum.server.api.util.ProtocolType;
import xyz.kvantum.server.api.util.TimeUtil;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.errors.ViewException;
import xyz.kvantum.server.api.views.requesthandler.HTTPSRedirectHandler;
import xyz.kvantum.server.implementation.error.KvantumException;

@RequiredArgsConstructor
final class ResponseTask implements Runnable
{

	private static final String HIDDEN_IP = "127.0.0.1";

	@NonNull final ChannelHandlerContext context;
	@NonNull final WorkerContext workerContext;

	@Override public void run()
	{
		try ( Timer.Context timer = KvantumServerHandler.TIMER_TOTAL_SEND.time() )
		{
			//
			// Attempt to find a handler for the request, or create
			// the appropriate error handler
			//
			determineRequestHandler();
			//
			// Generate the response
			//
			writeResponse();
			//
			// Send the response to the client
			//
			sendResponse( context );
		} catch ( final Throwable throwable )
		{
			handleThrowable( throwable, context );
		}
	}

	private void determineRequestHandler() throws Throwable
	{
		try ( Timer.Context timer = KvantumServerHandler.TIMER_ROUTING.time() )
		{
			workerContext.setRequestHandler( ServerImplementation.getImplementation().getRouter().match( workerContext.getRequest() ) );
			if ( workerContext.getRequestHandler() == null )
			{
				timer.close();
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
					timer.close();
					throw new ReturnStatus( Header.STATUS_INTERNAL_ERROR, workerContext, new SSLException(
							String.format( "Request handler %s forced HTTPS but SSL runner not enabled", workerContext.getRequestHandler() ) ) );
				}
				workerContext.setRequestHandler( HTTPSRedirectHandler.getInstance() );
			}
		}
	}

	void handleThrowable(@NonNull final Throwable throwable, @NonNull final ChannelHandlerContext context)
	{
		if ( throwable instanceof ReturnStatus )
		{
			try
			{
				final ReturnStatus returnStatus = ( ReturnStatus ) throwable;
				if ( returnStatus.getApplicableContext() == null )
				{
					returnStatus.setApplicableContext( this.workerContext );
				}

				// Here we need to decide whether ot not to do verbose logging or not
				final Response response;
				if ( CoreConfig.debug )
				{

					Message.WORKER_FAILED_HANDLING.log( throwable.getMessage() );

					if ( CoreConfig.verbose )
					{
						throwable.printStackTrace();
					}

					response = new ViewException( throwable ).generate( workerContext.getRequest() );
				} else
				{
					response = new Response();
					response.getHeader().clear();
					response.getHeader().set( Header.HEADER_CONTENT_LENGTH, AsciiString.of( 0 ) );
				}

				assert response != null && response.getResponseStream() != null;

				response.getHeader().setStatus( returnStatus.getStatus() );
				response.getHeader().set( Header.HEADER_CONNECTION, CLOSE );

				this.workerContext.setBody( response );
				this.workerContext.setResponseStream( response.getResponseStream() );
				this.sendResponse( context );
			} catch ( final Throwable innerThrowable )
			{
				new KvantumException( "Failed to handle return status", innerThrowable ).printStackTrace();
			}
		} else
		{
			new KvantumException( "Failed to handle incoming socket", throwable ).printStackTrace();
		}
	}

	private void writeResponse() throws Throwable
	{
		final Timer.Context timer = KvantumServerHandler.TIMER_WRITE_RESPONSE.time();
		//
		// Scope variables
		//
		RequestHandler requestHandler = workerContext.getRequestHandler();
		AbstractRequest request = workerContext.getRequest();
		ResponseBody body;
		ResponseStream responseStream;
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
				if ( redirect instanceof AbstractRequest )
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
				request.addMeta( KvantumServerHandler.CONTENT_TYPE, contentType.get().toString() );
			} else
			{
				request.addMeta( KvantumServerHandler.CONTENT_TYPE, null );
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
			/*
			Message.WORKER_FAILED_HANDLING.log( e.getMessage() );
			if ( CoreConfig.verbose )
			{
				e.printStackTrace();
			}

			if ( CoreConfig.debug )
			{
				body = new ViewException( e ).generate( request );
				responseStream = body.getResponseStream();
			} else
			{
				body = null;
			}
			*/
			timer.stop();
			throw new ReturnStatus( Header.STATUS_INTERNAL_ERROR, workerContext, e );
		}

		if ( responseStream == null )
		{
			timer.stop();
			throw new ReturnStatus( Header.STATUS_INTERNAL_ERROR, workerContext );
		}

		workerContext.setBody( body );
		workerContext.setResponseStream( responseStream );
		timer.stop();
	}

	@SuppressWarnings("ALL") private void sendResponse(final ChannelHandlerContext context)
	{
		final Timer.Context timer = KvantumServerHandler.TIMER_SEND_RESPONSE.time();

		//
		// Determine whether or not the response should be compressed
		//
		workerContext.determineGzipStatus();

		//
		// Get the generated body
		//
		ResponseBody body = workerContext.getBody();

		// Make sure that the generated response is valid (not null)
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
		if ( workerContext.getRequest().getHeaders().getOrDefault( KvantumServerHandler.CONNECTION, CLOSE )
				.equalsIgnoreCase( KEEP_ALIVE ) && !body.getHeader().get( Header.HEADER_CONNECTION ).orElse( KEEP_ALIVE ).equals( CLOSE ) )
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
		final Timer.Context timerWriteToClient = KvantumServerHandler.TIMER_WRITE_TO_CLIENT.time();

		final ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer( Buffer.out );

		//
		// Write the header
		//
		buf.writeBytes( body.getHeader().getFormat().getValue() );
		buf.writeBytes( KvantumServerHandler.SPACE );
		buf.writeBytes( body.getHeader().getStatus().getValue() );
		buf.writeBytes( KvantumServerHandler.NEW_LINE );
		for ( final Map.Entry<HeaderOption, AsciiString> entry : body.getHeader().getHeaders().entries() )
		{
			buf.writeBytes( entry.getKey().getBytes() );
			buf.writeBytes( KvantumServerHandler.COLON_SPACE );
			buf.writeBytes( entry.getValue().getValue() );
			buf.writeBytes( KvantumServerHandler.NEW_LINE );
		}
		// Print one empty line to indicate that the header sending is finished, this is important as the content
		// would otherwise be classed as headers, which really isn't optimal <3
		buf.writeBytes( KvantumServerHandler.NEW_LINE );

		//
		// Write the header to the client
		//
		context.write( buf );

		if ( CoreConfig.debug )
		{
			Logger.debug( "Using direct write from memory: {}", hasKnownLength );
		}

		int toRead;
		if ( hasKnownLength )
		{
			toRead = CoreConfig.Buffer.out;
		} else
		{
			toRead = CoreConfig.Buffer.out - KvantumServerHandler.MAX_LENGTH;
		}

		if ( toRead <= 0 )
		{
			Logger.warn( "buffer.out is less than {}, configured value will be ignored",
					KvantumServerHandler.MAX_LENGTH );
			toRead = MAX_LENGTH + 1;
		}

		long actualLength = 0L;

		//
		// Write the response
		//
		while ( !responseStream.isFinished() )
		{
			//
			// Read as much data as possible from the respone stream
			//
			byte[] bytes = responseStream.read( toRead );
			if ( bytes != null && bytes.length > 0 )
			{
				//
				// If the length is known, write data directly
				//
				if ( hasKnownLength )
				{
					context.write( bytes );
					actualLength = bytes.length;
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

					actualLength += bytes.length;

					context.write( AsciiString.of( Integer.toHexString( bytes.length ) ).getValue() );
					context.write( KvantumServerHandler.CRLF );
					context.write( bytes );
					context.write( KvantumServerHandler.CRLF );
					//
					// When using this mode we need to make sure that everything is written, so the
					// client doesn't time out
					//
					context.flush().newSucceededFuture().awaitUninterruptibly();
				}
			}
		}

		//
		// If we're using the chunked encoding format
		// write the end chunk
		//
		if ( !hasKnownLength )
		{
			context.write( KvantumServerHandler.END_CHUNK );
		}

		timerWriteToClient.stop();

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
		if ( CoreConfig.hideIps )
		{
			finalizedResponse.address( HIDDEN_IP );
		} else
		{
			finalizedResponse.address( this.workerContext.getSocketContext().getIP() );
		}

		finalizedResponse.authorization( this.workerContext.getRequest().getAuthorization().orElse( null ) )
				 .length( (int) actualLength ).status( body.getHeader().getStatus().toString() )
				.query( this.workerContext.getRequest().getQuery() ).timeFinished( System.currentTimeMillis() ).build();

		ServerImplementation.getImplementation().getEventBus().emit( finalizedResponse.build() );

		//
		// Make sure everything is written and either close the connection
		// or the channel (depending on whether keep-alive is used or not)
		//
		final ChannelFuture future = context.flush().newSucceededFuture();
		if ( !keepAlive )
		{
			future.addListener( ChannelFutureListener.CLOSE );
		}

		timer.stop();
	}

}
