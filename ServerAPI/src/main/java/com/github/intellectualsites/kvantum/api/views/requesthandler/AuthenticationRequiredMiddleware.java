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
package com.github.intellectualsites.kvantum.api.views.requesthandler;

import com.github.intellectualsites.kvantum.api.account.IAccountManager;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;

public class AuthenticationRequiredMiddleware extends Middleware
{

    @Override
    public void handle(AbstractRequest request, MiddlewareQueue queue)
    {
        final IAccountManager accountManager = ServerImplementation.getImplementation()
                .getApplicationStructure().getAccountManager();
        if ( accountManager != null && accountManager.getAccount( request.getSession() ).isPresent() )
        {
            queue.handle( request );
        } else
        {
            request.internalRedirect( CoreConfig.Middleware.loginRedirect );
        }
    }

}
