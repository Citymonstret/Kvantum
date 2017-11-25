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

import com.codahale.metrics.Timer;
import xyz.kvantum.nanotube.Transformer;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.ResponseBody;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.implementation.error.KvantumException;

import java.io.IOException;

/**
 * Sends the response back to the client
 */
final class ResponseSender extends Transformer<WorkerContext>
{

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        workerContext.determineGzipStatus();

        ResponseBody body = workerContext.getBody();
        byte[] bytes = workerContext.getBytes();

        if ( CoreConfig.contentMd5 )
        {
            final Md5Handler md5Handler = Server.md5HandlerPool.getNullable();

            Assert.notNull( bytes );
            Assert.notNull( body );
            Assert.notNull( body.getHeader() );

            body.getHeader().set( Header.HEADER_CONTENT_MD5, md5Handler.generateChecksum( bytes ) );
            Server.md5HandlerPool.add( md5Handler );
        }

        if ( workerContext.isGzip() )
        {
            final Timer.Context context = ServerImplementation.getImplementation().getMetrics().registerCompression();
            try
            {
                final GzipHandler gzipHandler = Server.gzipHandlerPool.getNullable();
                bytes = gzipHandler.compress( bytes );
                if ( body.getHeader().hasHeader( Header.HEADER_CONTENT_LENGTH ) )
                {
                    body.getHeader().set( Header.HEADER_CONTENT_LENGTH, "" + bytes.length );
                }
                Server.gzipHandlerPool.add( gzipHandler );
            } catch ( final IOException e )
            {
                new KvantumException( "( GZIP ) Failed to compress the bytes" ).printStackTrace();
            }
            context.stop();
        }

        //
        // Send the header to the client
        //
        body.getHeader().apply( workerContext.getOutput() );

        try
        {
            workerContext.getOutput().write( bytes );
        } catch ( final Exception e )
        {
            new KvantumException( "Failed to write to the client", e )
                    .printStackTrace();
        }

        //
        // Make sure everything is written
        //
        workerContext.flushOutput();

        //
        // Invalidate request to make sure that it isn't handled anywhere else, again (wouldn't work)
        //
        workerContext.getRequest().setValid( false );

        return workerContext;
    }
}
