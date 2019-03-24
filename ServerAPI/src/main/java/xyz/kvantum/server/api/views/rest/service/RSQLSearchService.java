/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
import xyz.kvantum.server.api.repository.KvantumRepository;
import xyz.kvantum.server.api.repository.MatcherFactory;
import xyz.kvantum.server.api.repository.RSQLMatcherFactory;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.api.views.RequestHandler;

import java.util.Collection;
import java.util.Optional;

@Getter @Builder @SuppressWarnings("ALL") public final class RSQLSearchService<ObjectType>
    implements SearchEngine {

    /**
     * URL Filter ({@link ViewPattern})
     */
    private final String filter;

    /**
     * Provider of search results, i.e {@link IAccountManager}
     */
    private final KvantumRepository<ObjectType, ?> resultProvider;

    /**
     * Matches queries to objects
     */
    private final MatcherFactory<String, ObjectType> matcher = new RSQLMatcherFactory<>();
    /**
     * String that will be used to get the query variable. Depends on the parameter type set for {@link
     * #parameterScope}.
     */
    private final String queryKey;
    /**
     * Whether GET or POST parameters will be used to read the object
     */
    @Builder.Default private ParameterScope parameterScope = ParameterScope.GET;
    /**
     * Can be used to set a permission that is required for the service to function. (See {@link AccountRole} and {@link
     * IAccount#isPermitted(String)})
     */
    @Builder.Default private String permissionRequirement = "";

    @Override public RequestHandler createService() {
        return ServerImplementation.getImplementation()
            .createSimpleRequestHandler(filter, ((request, response) -> {
                if (!getPermissionRequirement().isEmpty()) {
                    boolean hasPermission;
                    final ISession session = request.getSession();
                    if (session == null) {
                        hasPermission = false;
                    } else {
                        final Optional<IAccount> accountOptional =
                            ServerImplementation.getImplementation().getApplicationStructure()
                                .getAccountManager().getAccount(request.getSession());
                        hasPermission = accountOptional
                            .map(iAccount -> iAccount.isPermitted(getPermissionRequirement()))
                            .orElse(false);
                    }
                    if (!hasPermission) {
                        final JsonObject requestStatus = new JsonObject();
                        requestStatus.add("status", new JsonPrimitive("error"));
                        requestStatus.add("message", new JsonPrimitive("Not permitted"));
                        response.setResponse(ServerImplementation.getImplementation().getGson()
                            .toJson(requestStatus));
                        return;
                    }
                }

                response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_JSON);
                final String query;
                if (parameterScope == ParameterScope.GET) {
                    if (!request.getQuery().getParameters().containsKey(queryKey)) {
                        final JsonObject requestStatus = new JsonObject();
                        requestStatus.add("message",
                            new JsonPrimitive("No value for key " + queryKey + "was found"));
                        response.setResponse(ServerImplementation.getImplementation().getGson()
                            .toJson(requestStatus));
                        return;
                    } else {
                        query = request.getQuery().getParameters().get(queryKey);
                    }
                } else {
                    if (!request.getPostRequest().contains(queryKey)) {
                        final JsonObject requestStatus = new JsonObject();
                        requestStatus.add("message",
                            new JsonPrimitive("No value for key " + queryKey + "was found"));
                        response.setResponse(ServerImplementation.getImplementation().getGson()
                            .toJson(requestStatus));
                        return;
                    } else {
                        query = request.getPostRequest().get(queryKey);
                    }
                }
                final val matcher = getMatcher().createMatcher(query);
                final Collection<? extends ObjectType> queryResult =
                    resultProvider.findAllByQuery(matcher);
                if (queryResult.isEmpty()) {
                    final JsonObject requestStatus = new JsonObject();
                    requestStatus.add("status", new JsonPrimitive("error"));
                    requestStatus.add("message", new JsonPrimitive("No such object"));
                    requestStatus.add("query", new JsonPrimitive(query));
                    response.setResponse(
                        ServerImplementation.getImplementation().getGson().toJson(requestStatus));
                } else {
                    final JsonObject requestStatus = new JsonObject();
                    requestStatus.add("status", new JsonPrimitive("success"));
                    requestStatus.add("query", new JsonPrimitive(query));
                    final JsonArray resultArray = new JsonArray();
                    for (final ObjectType t : queryResult) {
                        resultArray
                            .add(ServerImplementation.getImplementation().getGson().toJsonTree(t));
                    }
                    requestStatus.add("result", resultArray);
                    response.setResponse(
                        ServerImplementation.getImplementation().getGson().toJson(requestStatus));
                }
            }));
    }

}
