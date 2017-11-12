/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.views.rest;

import com.github.intellectualsites.kvantum.api.orm.KvantumObjectFactory;
import com.github.intellectualsites.kvantum.api.util.ParameterScope;
import com.github.intellectualsites.kvantum.api.util.SearchResultProvider;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import com.github.intellectualsites.kvantum.api.views.rest.service.SearchService;
import lombok.experimental.UtilityClass;

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
     * </p>
     * <p>
     * Example: <pre>{@code
     * Rest.createSearch(
     *    "/search",
     *    Account.class,
     *    ParameterScope.GET,
     *    ServerImplementation.getImplementation().getApplicationStructure().getAccountManager()
     * );}</pre>
     * </p>
     * <p>
     * To limit the request to certain roles, you should instead use {@link SearchService#builder()} and
     * then {@link SearchService.SearchServiceBuilder#permissionRequirement(String)}
     * </p>
     * @param filter URL Filter ({@link com.github.intellectualsites.kvantum.api.matching.ViewPattern})
     * @param clazz Class, must comply to KvantumObject specifications (see {@link KvantumObjectFactory})
     * @param scope  Whether GET or POST parameters will be used to read the object
     * @param resultProvider Provider of search results, i.e {@link com.github.intellectualsites.kvantum.api.account.IAccountManager}
     * @param <T> Lowest class type
     * @return The created request handler
     */
    public static <T> RequestHandler createSearch(final String filter, final Class<? extends T> clazz,
                                                  final ParameterScope scope, final SearchResultProvider<T> resultProvider)
    {
        return SearchService.<T>builder()
                .filter( filter )
                .resultProvider( resultProvider )
                .objectType( clazz )
                .parameterScope( scope )
                .build().registerHandler();
    }

}
