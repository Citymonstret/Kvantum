/*
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import xyz.kvantum.server.api.cache.CacheApplicable;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.core.WorkerProcedure;
import xyz.kvantum.server.api.events.ConnectionEstablishedEvent;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.Request;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.HeaderOption;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.response.ResponseBody;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.DebugTree;
import xyz.kvantum.server.api.util.ProtocolType;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.errors.ViewException;
import xyz.kvantum.server.api.views.requesthandler.HTTPSRedirectHandler;
import xyz.kvantum.server.implementation.error.KvantumException;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
final class KvantumServerHandler extends ChannelInboundHandlerAdapter
{

    private static final byte[] EMPTY = "NULL".getBytes( StandardCharsets.UTF_8 );
    private static final String CONTENT_TYPE = "content_type";
    private static final byte[] NEW_LINE = "\n".getBytes( StandardCharsets.UTF_8 );

    private final ProtocolType protocolType;

    private ByteBuf byteBuf;
    private WorkerContext workerContext;
    private RequestReader requestReader;

    @Override
    public void handlerAdded(final ChannelHandlerContext context)
    {
        this.byteBuf = context.alloc().buffer();

        //
        // Prepare request
        //
        final Supplier<Boolean> activeCheck = () -> context.channel().isOpen() && context.channel().isActive();
        final SocketAddress remoteAddress = context.channel().remoteAddress();
        SocketContext socketContext = new SocketContext( this.protocolType, remoteAddress, activeCheck );
        this.workerContext = new WorkerContext( ServerImplementation.getImplementation(), ServerImplementation.getImplementation()
                .getProcedure().getInstance(), this );
        this.workerContext.setSocketContext( socketContext );
        final Request request = new Request( socketContext );
        this.workerContext.setRequest( request );
        this.requestReader = new RequestReader( request );

        Message.CONNECTION_ACCEPTED.log( workerContext.getSocketContext().getAddress() );
    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext context)
    {
        this.byteBuf.release();
        this.byteBuf = null;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception
    {
        final ConnectionEstablishedEvent connectionEstablishedEvent = new ConnectionEstablishedEvent(
                this.workerContext.getSocketContext().getIP() );
        ServerImplementation.getImplementation().getEventBus().emit( connectionEstablishedEvent );
        if ( connectionEstablishedEvent.isCancelled() )
        {
            ctx.close();
        }
        super.channelActive( ctx );
    }

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object messageObject)
    {
        final ByteBuf message = (ByteBuf) messageObject;
        this.byteBuf.writeBytes( message );
        message.release();
        try
        {
            this.requestReader.readBytes( this.byteBuf );
        } catch ( final Throwable throwable )
        {
            this.handleThrowable( throwable, context );
        }
        if ( this.requestReader.isDone() )
        {
            // Release content
            this.requestReader.clear();
            this.workerContext.getRequest().onCompileFinish();
            if ( CoreConfig.debug )
            {
                this.workerContext.getRequest().dumpRequest();
            }
            if ( CoreConfig.Sessions.autoLoad )
            {
                workerContext.getRequest().requestSession();
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
            final ReturnStatus returnStatus = (ReturnStatus) throwable;
            if ( returnStatus.getApplicableContext() == null )
            {
                returnStatus.setApplicableContext( this.workerContext );
            }

            final Response response = new Response();
            response.getHeader().clear();
            response.getHeader().setStatus( returnStatus.getStatus() );
            response.getHeader().set( Header.HEADER_CONNECTION, "close" );

            this.workerContext.setBody( response );
            this.workerContext.setBytes( response.getBytes() );
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
        String textContent = "";
        byte[] bytes = EMPTY;
        boolean shouldCache = false;
        boolean cache = false;
        RequestHandler requestHandler = workerContext.getRequestHandler();
        AbstractRequest request = workerContext.getRequest();
        ResponseBody body;

        try
        {
            if ( !requestHandler.getValidationManager().isEmpty() )
            {
                requestHandler.getValidationManager().validate( request );
            }

            //
            // Try to find cached response
            //
            if ( CoreConfig.Cache.enabled && requestHandler instanceof CacheApplicable
                    && ( (CacheApplicable) requestHandler ).isApplicable( request ) )
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
            } else
            { // Just read from memory
                body = ServerImplementation.getImplementation().getCacheManager().getCache( requestHandler );
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
                    final AbstractRequest redirectRequest = (AbstractRequest) redirect;
                    redirectRequest.removeMeta( "internalRedirect" );
                    workerContext.setRequest( redirectRequest );
                    this.writeResponse();
                }
                return;
            }

            //
            // Store cache
            //
            if ( shouldCache )
            {
                ServerImplementation.getImplementation().getCacheManager().setCache( requestHandler, body );
            }

            if ( body.isText() )
            {
                textContent = body.getContent();
            } else
            {
                bytes = body.getBytes();
            }

            //
            // Post-generation procedures
            //
            request.postponedCookies.forEach( body.getHeader()::setCookie );

            //
            // Add the content type meta to the request, can then be used by worker processes
            //
            final Optional<String> contentType = body.getHeader().get( Header.HEADER_CONTENT_TYPE );
            if ( contentType.isPresent() )
            {
                request.addMeta( CONTENT_TYPE, contentType.get() );
            } else
            {
                request.addMeta( CONTENT_TYPE, null );
            }

            //
            // Turn response into a byte array
            //
            if ( request.getQuery().getMethod().hasBody() )
            {
                if ( body.isText() )
                {
                    for ( final WorkerProcedure.Handler<String> handler : workerContext.getWorkerProcedureInstance()
                            .getStringHandlers() )
                    {
                        textContent = handler.act( requestHandler, request, textContent );
                    }
                    bytes = textContent.getBytes( StandardCharsets.UTF_8 );
                }

                if ( !workerContext.getWorkerProcedureInstance().getByteHandlers().isEmpty() )
                {
                    Byte[] wrapper = ArrayUtils.toObject( bytes );
                    for ( final WorkerProcedure.Handler<Byte[]> handler : workerContext.getWorkerProcedureInstance()
                            .getByteHandlers() )
                    {
                        wrapper = handler.act( requestHandler, request, wrapper );
                    }
                    bytes = ArrayUtils.toPrimitive( wrapper );
                }
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
                bytes = body.getContent().getBytes();
            } else
            {
                body = null;
            }
        }

        if ( body == null )
        {
            throw new ReturnStatus( Header.STATUS_INTERNAL_ERROR, workerContext );
        }

        workerContext.setBody( body );
        workerContext.setBytes( bytes );
    }

    private void handleResponse(final ChannelHandlerContext context) throws Throwable
    {
        workerContext.setRequestHandler( ServerImplementation.getImplementation().getRouter().match( workerContext.getRequest() ) );
        if ( workerContext.getRequestHandler() == null )
        {
            throw new ReturnStatus( Header.STATUS_NOT_FOUND, workerContext );
        }
        if ( workerContext.getRequest().getProtocolType() != ProtocolType.HTTPS && workerContext.getRequestHandler()
                .forceHTTPS() )
        {
            if ( CoreConfig.debug )
            {
                Logger.debug( "Redirecting request [%s] to HTTPS version of [%s]", workerContext.getRequest(),
                        workerContext.getRequestHandler() );
            }
            if ( !CoreConfig.SSL.enable )
            {
                Logger.error( "RequestHandler (%s) forces HTTPS but SSL runner not enabled!" );
                throw new ReturnStatus( Header.STATUS_INTERNAL_ERROR, workerContext );
            }
            workerContext.setRequestHandler( HTTPSRedirectHandler.getInstance() );
        }
        this.writeResponse();
        this.sendResponse( context );
    }

    @SuppressWarnings("ALL")
    private void sendResponse(final ChannelHandlerContext context)
    {
        workerContext.determineGzipStatus();

        ResponseBody body = workerContext.getBody();
        byte[] bytes = workerContext.getBytes();

        if ( CoreConfig.contentMd5 )
        {
            final Md5Handler md5Handler = SimpleServer.md5HandlerPool.getNullable();

            Assert.notNull( bytes );
            Assert.notNull( body );
            Assert.notNull( body.getHeader() );

            final String checksum = md5Handler.generateChecksum( bytes );

            body.getHeader().set( Header.HEADER_CONTENT_MD5, checksum );
            body.getHeader().set( Header.HEADER_ETAG, checksum );

            SimpleServer.md5HandlerPool.add( md5Handler );
        }

        if ( workerContext.isGzip() )
        {
            try
            {
                final GzipHandler gzipHandler = SimpleServer.gzipHandlerPool.getNullable();
                bytes = gzipHandler.compress( bytes );
                if ( body.getHeader().hasHeader( Header.HEADER_CONTENT_LENGTH ) )
                {
                    body.getHeader().set( Header.HEADER_CONTENT_LENGTH, "" + bytes.length );
                }
                SimpleServer.gzipHandlerPool.add( gzipHandler );
            } catch ( final IOException e )
            {
                new KvantumException( "( GZIP ) Failed to compress the bytes" ).printStackTrace();
            }
        }

        if ( CoreConfig.debug )
        {
            DebugTree.builder().name( "Response Information" )
                    .entry( "Address", workerContext.getSocketContext().getAddress() )
                    .entry( "Headers", body.getHeader().getHeaders() ).build().collect()
                    .forEach( Logger::debug );
        }

        final ByteBuf buf = context.alloc().buffer();

        //
        // Send the header to the client
        //
        buf.writeBytes( ( body.getHeader().getFormat() + " " + body.getHeader().getStatus() + "\n" )
                .getBytes( StandardCharsets.UTF_8 ) );
        for ( final Map.Entry<HeaderOption, String> entry : body.getHeader().getHeaders().entries() )
        {
            buf.writeBytes( ( entry.getKey() + ": " + entry.getValue() + "\n" ).getBytes( StandardCharsets.UTF_8 ) );
        }
        // Print one empty line to indicate that the header sending is finished, this is important as the content
        // would otherwise be classed as headers, which really isn't optimal <3
        buf.writeBytes( NEW_LINE );

        // Write body
        buf.writeBytes( bytes );

        //
        // Make sure everything is written
        //
        final ChannelFuture future = context.writeAndFlush( buf );
        future.addListener( (ChannelFutureListener) f -> context.close() );

        //
        // Invalidate request to make sure that it isn't handled anywhere else, again (wouldn't work)
        //
        workerContext.getRequest().setValid( false );
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause)
    {
        cause.printStackTrace();
        context.close();
    }
}
