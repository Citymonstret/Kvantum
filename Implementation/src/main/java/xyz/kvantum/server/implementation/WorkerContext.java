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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.core.WorkerProcedure;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.ResponseBody;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.api.views.RequestHandler;

@Getter
@Setter
@RequiredArgsConstructor
public class WorkerContext
{

    private static final String CONTENT_TYPE = "content_type";
    private static byte[] EMPTY = "NULL".getBytes();

    private final Kvantum server;
    private final WorkerProcedure.WorkerProcedureInstance workerProcedureInstance;

    private final KvantumServerHandler kvantumServerHandler;

    private RequestHandler requestHandler;
    private AbstractRequest request;
    private ResponseBody body;
    private boolean gzip;
    private SocketContext socketContext;
    private byte[] bytes;

    /**
     * <p>
     * Determine whether or not GZIP compression should be used.
     * This depends on two things:
     * <ol>
     * <li>If GZIP compression is enabled in {@link CoreConfig}</li>
     * <li>If the client has sent a "Accept-Encoding" header</li>
     * </ol>
     * </p>
     * <p>
     * The value can be fetched using {@link #isGzip()}
     * </p>
     */
    void determineGzipStatus()
    {
        if ( CoreConfig.gzip )
        {
            if ( request.getHeader( "Accept-Encoding" ).contains( "gzip" ) )
            {
                this.gzip = true;
                body.getHeader().set( Header.HEADER_CONTENT_ENCODING, "gzip" );
            } else if ( CoreConfig.debug )
            {
                Message.CLIENT_NOT_ACCEPTING_GZIP.log( request.getHeaders() );
            }
        } else
        {
            this.gzip = false;
        }
    }

}
