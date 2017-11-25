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

import xyz.kvantum.nanotube.ConditionalTransformer;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;

/**
 * Logs {@link Message#CONNECTION_ACCEPTED} if {@link CoreConfig#verbose} is toggled
 */
final class ConnectionLogger extends ConditionalTransformer<WorkerContext>
{

    ConnectionLogger()
    {
        super( ignore -> CoreConfig.verbose );
    }

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        Message.CONNECTION_ACCEPTED.log( workerContext.getSocketContext().getAddress() );
        return workerContext;
    }

}
