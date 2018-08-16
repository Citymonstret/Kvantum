/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.views.requesthandler;

import lombok.NonNull;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.request.AbstractRequest;

public final class AuthenticationRequiredMiddleware extends Middleware
{

	@Override public void handle(@NonNull final AbstractRequest request, @NonNull final MiddlewareQueue queue)
	{
		final IAccountManager accountManager = ServerImplementation.getImplementation().getApplicationStructure()
				.getAccountManager();
		if ( accountManager != null && accountManager.getAccount( request.getSession() ).isPresent() )
		{
			queue.handle( request );
		} else
		{
			request.internalRedirect( CoreConfig.Middleware.loginRedirect );
		}
	}

}
