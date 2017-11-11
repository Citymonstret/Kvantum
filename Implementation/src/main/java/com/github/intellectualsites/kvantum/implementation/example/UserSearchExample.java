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
        Logger.info( "INITIALIZING EXAMPLE: UserSearch" );
        Rest.createSearch( "/search", Account.class, ParameterScope.GET, ServerImplementation.getImplementation()
                .getApplicationStructure().getAccountManager() );
        Logger.info( "ACCESS THE EXAMPLE AT: /search?username=<username>&id=<id>" );
        Logger.info( "EXAMPLE: /search?username=admin" );
    }

}
