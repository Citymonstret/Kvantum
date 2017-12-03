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
import xyz.kvantum.server.api.request.Request;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Class responsible for reading HTTP requests
 */
final class SocketRequestReader extends Transformer<WorkerContext>
{

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        if ( workerContext.getRequest() != null )
        {
            return workerContext;
        }
        final Timer.Context readInput = ServerImplementation.getImplementation().getMetrics().registerReadInput();
        workerContext.setRequest( new Request( workerContext.getSocketContext() ) );
        final BlockingSocketReader socketReader = new BlockingSocketReader( workerContext.getSocketContext(),
                new RequestReader( workerContext.getRequest() ) );
        while ( !socketReader.isDone() )
        {
            socketReader.tick();
        }
        workerContext.getRequest().onCompileFinish();
        workerContext.setOutput( new BufferedOutputStream( workerContext.getSocketContext().getSocket()
                .getOutputStream(), CoreConfig.Buffer.out ) );
        workerContext.getRequest().setInputReader( new BufferedReader(
                new InputStreamReader( socketReader.getInputStream() ) ) );
        workerContext.getRequest().setOutputStream( workerContext.getOutput() );
        readInput.stop();
        return workerContext;
    }
}
