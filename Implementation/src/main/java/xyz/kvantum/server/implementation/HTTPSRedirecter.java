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
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.util.ProtocolType;
import xyz.kvantum.server.api.views.requesthandler.HTTPSRedirectHandler;

/**
 * Redirects requests to HTTPS, if this is desired
 */
final class HTTPSRedirecter extends ConditionalTransformer<WorkerContext>
{

    HTTPSRedirecter()
    {
        super( workerContext -> workerContext.getRequest().getProtocolType() != ProtocolType.HTTPS && workerContext
                .getRequestHandler().forceHTTPS() );
    }

    @Override
    protected WorkerContext handle(final WorkerContext workerContext) throws Throwable
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
        return workerContext;
    }
}
