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
package xyz.kvantum.server.api.views.rest;

import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.account.IAccountManager;
import xyz.kvantum.server.api.matching.ViewPattern;
import xyz.kvantum.server.api.orm.KvantumObjectFactory;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.api.util.SearchResultProvider;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.rest.service.SearchService;

/**
 * Utility methods that will aid in the creation of RESTful services
 */
@SuppressWarnings( "ALL" )
@UtilityClass
public class Rest
{

    /**
     * Wrapper method for {@link SearchService}
     * <p>
     * Create a REST search gateway that will search within a specified datastore for Kvantum
     * objects and automatically serve them via JSON.

     * Example: <pre>{@code
     * Rest.createSearch(
     *    "/search",
     *    Account.class,
     *    ParameterScope.GET,
     *    ServerImplementation.getImplementation().getApplicationStructure().getAccountManager()
     * );}</pre>

     * To limit the request to certain roles, you should instead use {@link SearchService#builder()} and
     * then {@link SearchService.SearchServiceBuilder#permissionRequirement(String)}
     *
     * @param filter URL Filter ({@link ViewPattern})
     * @param clazz Class, must comply to KvantumObject specifications (see {@link KvantumObjectFactory})
     * @param scope  Whether GET or POST parameters will be used to read the object
     * @param resultProvider Provider of search results, i.e {@link IAccountManager}
     * @param <T> Lowest class type
     * @return The created request handler
     */
    public static <QueryType, ObjectType> RequestHandler createSearch(
            final String filter,
            final Class<? extends QueryType> clazz,
            final ParameterScope scope,
            final SearchResultProvider<QueryType, ObjectType> resultProvider)
    {
        return SearchService.<QueryType, ObjectType>builder()
                .filter( filter )
                .resultProvider( resultProvider )
                .queryObjectType( clazz )
                .parameterScope( scope )
                .build().registerHandler();
    }

}
