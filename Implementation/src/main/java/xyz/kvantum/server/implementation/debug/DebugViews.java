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
package xyz.kvantum.server.implementation.debug;

import lombok.val;
import org.json.simple.JSONObject;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.orm.KvantumObjectFactory;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.api.views.staticviews.StaticViewManager;
import xyz.kvantum.server.api.views.staticviews.ViewMatcher;
import xyz.kvantum.server.api.views.staticviews.converters.StandardConverters;
import xyz.kvantum.server.implementation.Session;

import java.util.Optional;

public final class DebugViews
{

    private static final DebugViews instance = new DebugViews();
    private static final KvantumPojoFactory<Session> sessionPojoFactory = KvantumPojoFactory.forClass( Session.class );

    private DebugViews()
    {
    }

    public static void registerDebugViews()
    {
        if ( !CoreConfig.debug )
        {
            return;
        }
        try
        {
            StaticViewManager.generate( instance );
            Logger.debug( "Registered debug views: " );
            Logger.debug( "- debug/session?debug=true : See your session ID" );
            Logger.debug( "- debug/accounts?username=<username>&password=<password> : Test accounts" );
        } catch ( final Exception e )
        {
            Logger.error( "Failed to generate debug views!" );
            e.printStackTrace();
        }
    }

    @ViewMatcher(filter = "debug/string", outputType = StandardConverters.HTML)
    public final String testString(final AbstractRequest request)
    {
        return "<h1>Hello World!</h1>";
    }

    @ViewMatcher(filter = "debug/json", outputType = StandardConverters.JSON)
    public final JSONObject testJson(final AbstractRequest request)
    {
        final JSONObject object = new JSONObject();
        object.put( "Hello", "World" );
        return object;
    }

    @ViewMatcher(filter = "debug/accounts", httpMethod = HttpMethod.GET)
    public final void debugAccounts(final AbstractRequest request, final Response response)
    {
        final IAccountManager accountManager = ServerImplementation.getImplementation().getApplicationStructure()
                .getAccountManager();
        Optional<IAccount> accountOptional;
        if ( ( accountOptional = accountManager.getAccount( request.getSession() ) ).isPresent() )
        {
            response.setContent( "You are already logged in..." );
        } else
        {
            final KvantumObjectFactory<DebugLoginAttempt> factory = KvantumObjectFactory.from( DebugLoginAttempt
                    .class );
            final val result = factory.build( ParameterScope.GET ).parseRequest( request );
            if ( result.isSuccess() )
            {
                accountOptional = accountManager.getAccount( result.getParsedObject().getUsername() );
                if ( accountOptional.isPresent() )
                {
                    if ( accountOptional.get().passwordMatches( result.getParsedObject().getPassword() ) )
                    {
                        accountManager.bindAccount( accountOptional.get(), request.getSession() );
                        response.setContent( "Success!" );
                    } else
                    {
                        response.setContent( "Password is wrong!" );
                    }
                } else
                {
                    response.setContent( "No such account..." );
                }
            } else
            {
                response.setContent( "ERROR: " + result.getError().getCause() );
            }
        }
    }

    @ViewMatcher(filter = "debug/session/json", outputType = "json")
    public final JSONObject debugSessionJson(final AbstractRequest request)
    {
        return sessionPojoFactory.of( (Session) request.getSession() ).toJson();
    }

    @ViewMatcher(filter = "debug/session", name = "debugSession", httpMethod = HttpMethod.GET,
            middlewares = { DebugRedirectMiddleware.class })
    public final void debugSession(final AbstractRequest request, final Response response)
    {
        request.requestSession();
        response.setContent( "<h1><b>Session:</b> " + request.getSession().get( "id" ) + "</h1>" );
    }

}
