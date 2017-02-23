/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.views.requesthandler;

import com.plotsquared.iserver.account.AccountManager;
import com.plotsquared.iserver.object.Request;

import java.util.Optional;

public class AuthenticationRequiredMiddleware extends Middleware
{

    @Override
    public void handle(Request request, MiddlewareQueue queue)
    {
        final Optional<AccountManager> accountManager = com.plotsquared.iserver.core.ServerImplementation.getImplementation().getAccountManager();
        if ( accountManager.isPresent() && accountManager.get().getAccount( request.getSession() ).isPresent() )
        {
            queue.handle( request );
        } else
        {
            request.internalRedirect( "login" );
        }
    }

}
