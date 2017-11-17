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
package com.github.intellectualsites.kvantum.implementation.example;

import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.util.ParameterScope;
import com.github.intellectualsites.kvantum.api.views.rest.Rest;
import com.github.intellectualsites.kvantum.implementation.Account;

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
