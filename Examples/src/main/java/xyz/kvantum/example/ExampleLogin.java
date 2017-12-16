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
package xyz.kvantum.example;

import xyz.kvantum.example.object.DebugLoginAttempt;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.orm.KvantumObjectFactory;
import xyz.kvantum.server.api.orm.KvantumObjectParserResult;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.api.views.staticviews.StaticViewManager;
import xyz.kvantum.server.api.views.staticviews.ViewMatcher;

import java.util.Optional;

/**
 * Send a POST request to "/login",
 * required fields: username, password
 * <p>
 * Default admin account is Username: admin Password: admin
 */
@SuppressWarnings("unused")
class ExampleLogin
{

    ExampleLogin()
    {
        try
        {
            StaticViewManager.generate( this );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    @ViewMatcher(filter = "login", httpMethod = HttpMethod.POST)
    public final void debugAccounts(final AbstractRequest request, final Response response)
    {
        final IAccountManager accountManager = ServerImplementation.getImplementation().getApplicationStructure()
                .getAccountManager();
        if ( accountManager.getAccount( request.getSession() ).isPresent() )
        {
            response.setContent( "You are already logged in..." );
            return;
        }

        final KvantumObjectFactory<DebugLoginAttempt> factory = KvantumObjectFactory.from( DebugLoginAttempt
                .class );
        final KvantumObjectParserResult<DebugLoginAttempt> result
                = factory.build( ParameterScope.POST ).parseRequest( request );

        if ( !result.isSuccess() )
        {
            response.setContent( "Error: " + result.getError().getCause() );
            return;
        }

        final Optional<IAccount> accountOptional = accountManager.getAccount( result.getParsedObject().getUsername() );
        if ( !accountOptional.isPresent() )
        {
            response.setContent( "No such account..." );
            return;
        }

        final IAccount account = accountOptional.get();
        if ( account.passwordMatches( result.getParsedObject().getPassword() ) )
        {
            accountManager.bindAccount( account, request.getSession() );
            response.setContent( "Success!" );
        } else
        {
            response.setContent( "Password is wrong!" );
        }
    }

}
