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

import xyz.kvantum.nanotube.Transformer;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.response.Header;

/**
 * Makes sure there aren't too many lines in the request
 */
final class RequestLineValidator extends Transformer<WorkerContext>
{

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
    {
        if ( workerContext.getLines().size() > CoreConfig.Limits.limitRequestLines )
        {
            throw new ReturnStatus( Header.STATUS_PAYLOAD_TOO_LARGE, workerContext );
        }
        return workerContext;
    }
}
