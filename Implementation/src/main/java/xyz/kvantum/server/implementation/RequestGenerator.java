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
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.ProtocolNotSupportedException;
import xyz.kvantum.server.api.exceptions.QueryException;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.Request;
import xyz.kvantum.server.api.response.Header;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Generates a {@link AbstractRequest}
 * from a {@link WorkerContext}
 */
final class RequestGenerator extends Transformer<WorkerContext>
{

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        try
        {
            final Timer.Context metricContext = ServerImplementation.getImplementation().getMetrics()
                    .registerRequestPreparation();
            workerContext.setRequest( new Request( workerContext.getLines(), workerContext.getSocketContext() ) );
            metricContext.stop();
        } catch ( final ProtocolNotSupportedException ex )
        {
            throw new ReturnStatus( Header.STATUS_HTTP_VERSION_NOT_SUPPORTED, workerContext );
        } catch ( final QueryException ex )
        {
            Logger.error( "Failed to read query (%s)", ex.getMessage() );
            throw new ReturnStatus( Header.STATUS_BAD_REQUEST, workerContext );
        } catch ( final Exception ex )
        {
            ex.printStackTrace();
            throw new ReturnStatus( Header.STATUS_BAD_REQUEST, workerContext );
        }
        workerContext.getRequest().setInputReader( new BufferedReader(
                new InputStreamReader( workerContext.getInputStream() ) ) );
        workerContext.getRequest().setOutputStream( workerContext.getOutput() );
        return workerContext;
    }
}
