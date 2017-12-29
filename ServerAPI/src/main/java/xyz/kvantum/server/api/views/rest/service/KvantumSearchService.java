/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.server.api.views.rest.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Builder;
import lombok.Getter;
import lombok.val;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.account.roles.AccountRole;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.matching.ViewPattern;
import xyz.kvantum.server.api.orm.KvantumObjectFactory;
import xyz.kvantum.server.api.repository.KvantumRepository;
import xyz.kvantum.server.api.repository.MatcherFactory;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.api.views.RequestHandler;

import java.util.Collection;
import java.util.Optional;

@Getter
@Builder
public final class KvantumSearchService<QueryType, ObjectType> implements SearchEngine
{

    /**
     * URL Filter ({@link ViewPattern})
     */
    private final String filter;

    /**
     * Class, must comply to KvantumObject specifications (see {@link KvantumObjectFactory})
     */
    private final Class<? extends QueryType> queryObjectType;

    /**
     * Provider of search results, i.e {@link IAccountManager}
     */
    private final KvantumRepository<ObjectType, ?> resultProvider;

    /**
     * Matches queries to objects
     */
    private final MatcherFactory<QueryType, ObjectType> matcher;

    /**
     * Whether GET or POST parameters will be used to read the object
     */
    @Builder.Default
    private ParameterScope parameterScope = ParameterScope.GET;

    /**
     * Can be used to set a permission that is required for the service to function.
     * (See {@link AccountRole} and
     * {@link IAccount#isPermitted(String)})
     */
    @Builder.Default
    private String permissionRequirement = "";

    @Override
    public RequestHandler createService()
    {
        return ServerImplementation.getImplementation().createSimpleRequestHandler( filter, ( (request, response) ->
        {
            if ( !getPermissionRequirement().isEmpty() )
            {
                boolean hasPermission;
                final ISession session = request.getSession();
                if ( session == null )
                {
                    hasPermission = false;
                } else
                {
                    final Optional<IAccount> accountOptional = ServerImplementation.getImplementation()
                            .getApplicationStructure().getAccountManager().getAccount( request.getSession() );
                    hasPermission = accountOptional.map( iAccount ->
                            iAccount.isPermitted( getPermissionRequirement() ) ).orElse( false );
                }
                if ( !hasPermission )
                {
                    final JsonObject requestStatus = new JsonObject();
                    requestStatus.add( "status", new JsonPrimitive( "error" ) );
                    requestStatus.add( "message", new JsonPrimitive( "Not permitted" ) );
                    response.setContent( ServerImplementation.getImplementation().getGson().toJson( requestStatus ) );
                    return;
                }
            }

            final val factory = KvantumObjectFactory.from( queryObjectType ).build( parameterScope );
            final val result = factory.parseRequest( request );
            response.getHeader().set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_JSON );
            if ( !result.isSuccess() )
            {
                final JsonObject requestStatus = new JsonObject();
                requestStatus.add( "message", new JsonPrimitive( result.getError().getCause() ) );
                response.setContent( ServerImplementation.getImplementation().getGson().toJson( requestStatus ) );
                return;
            }
            final QueryType query = result.getParsedObject();
            final val matcher = getMatcher().createMatcher( query );
            final Collection<? extends ObjectType> queryResult = resultProvider.findAllByQuery( matcher );
            if ( queryResult.isEmpty() )
            {
                final JsonObject requestStatus = new JsonObject();
                requestStatus.add( "status", new JsonPrimitive( "error" ) );
                requestStatus.add( "message", new JsonPrimitive( "No such object" ) );
                requestStatus.add( "query", ServerImplementation.getImplementation()
                        .getGson().toJsonTree( query ) );
                response.setContent( ServerImplementation.getImplementation().getGson().toJson( requestStatus ) );
            } else
            {
                final JsonObject requestStatus = new JsonObject();
                requestStatus.add( "status", new JsonPrimitive( "success" ) );
                requestStatus.add( "query", ServerImplementation.getImplementation()
                        .getGson().toJsonTree( query ) );
                final JsonArray resultArray = new JsonArray();
                for ( final ObjectType t : queryResult )
                {
                    resultArray.add( ServerImplementation.getImplementation()
                            .getGson().toJsonTree( t ) );
                }
                requestStatus.add( "result", resultArray );
                response.setContent( ServerImplementation.getImplementation().getGson().toJson( requestStatus ) );
            }
        } ) );
    }

}
