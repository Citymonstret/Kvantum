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
package xyz.kvantum.server.implementation.example;

import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.api.views.rest.Rest;
import xyz.kvantum.server.implementation.Account;

class UserSearchExample implements Example
{

    @Override
    public void initExample()
    {
        Logger.info( "" );
        Logger.info( "INITIALIZING EXAMPLE: UserSearch" );
        Rest.createSearch( "/search", Account.class, ParameterScope.GET, ServerImplementation.getImplementation()
                .getApplicationStructure().getAccountManager() );
        Logger.info( "ACCESS THE EXAMPLE AT: /search?username=<username>&id=<id>" );
        Logger.info( "EXAMPLE: /search?username=admin" );
        Logger.info( "" );
    }

}
