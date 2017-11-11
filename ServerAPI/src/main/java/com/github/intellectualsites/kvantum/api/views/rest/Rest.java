package com.github.intellectualsites.kvantum.api.views.rest;

import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.orm.KvantumObjectFactory;
import com.github.intellectualsites.kvantum.api.response.Header;
import com.github.intellectualsites.kvantum.api.util.ParameterScope;
import com.github.intellectualsites.kvantum.api.util.SearchResultProvider;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collection;

/**
 * Utility methods that will aid in the creation of RESTful services
 */
@UtilityClass
public class Rest
{

    /**
     * Create a REST search gateway that will search within a specified datastore for Kvantum
     * objects and automatically serve them via JSON.
     * <p>
     * Example: <pre>{@code
     * Rest.createSearch(
     *    "/search",
     *    Account.class,
     *    ParameterScope.GET,
     *    ServerImplementation.getImplementation().getApplicationStructure().getAccountManager();
     * );}</pre>
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
        return ServerImplementation.getImplementation().createSimpleRequestHandler( filter, ( (request, response) ->
        {
            final val factory = KvantumObjectFactory.from( clazz ).build( scope );
            final val result = factory.parseRequest( request );
            response.getHeader().set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_JSON );
            if ( !result.isSuccess() )
            {
                final JsonObject requestStatus = new JsonObject();
                requestStatus.add( "message", new JsonPrimitive( result.getError().getCause() ) );
                response.setContent( ServerImplementation.getImplementation().getGson().toJson( requestStatus ) );
            }
            final T query = result.getParsedObject();
            final Collection<? extends T> queryResult = resultProvider.getResults( query );
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
                for ( final T t : queryResult )
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
