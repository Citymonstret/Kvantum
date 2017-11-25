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

import java.io.BufferedOutputStream;

/**
 * Class responsible for reading HTTP requests
 */
final class SocketRequestReader extends Transformer<WorkerContext>
{

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        final Timer.Context readInput = ServerImplementation.getImplementation().getMetrics().registerReadInput();
        final BlockingSocketReader socketReader = new BlockingSocketReader( workerContext.getSocketContext(), new RequestReader() );
        workerContext.setOutput( new BufferedOutputStream( workerContext.getSocketContext().getSocket()
                .getOutputStream(), CoreConfig.Buffer.out ) );
        while ( !socketReader.isDone() )
        {
            socketReader.tick();
        }
        readInput.stop();
        workerContext.setLines( socketReader.getLines() );
        workerContext.setInputStream( socketReader.getInputStream() );
        return workerContext;
    }
}
