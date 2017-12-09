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
package xyz.kvantum.server.api.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.QueryException;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.CookieManager;
import xyz.kvantum.server.api.util.ProtocolType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final public class Request extends AbstractRequest
{

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private boolean hasBeenRequested = false;

    public Request(final SocketContext socket)
    {
        Assert.notNull( socket );
        this.setSocket( socket );
        if ( socket.isSSL() )
        {
            this.setProtocolType( ProtocolType.HTTPS );
        } else
        {
            this.setProtocolType( ProtocolType.HTTP );
        }
    }

    @Override
    public void onCompileFinish()
    {
        if ( this.getQuery() == null )
        {
            throw new QueryException( "Couldn't find query header...", this );
        }
        this.setCookies( CookieManager.getCookies( this ) );
        if ( this.getHeaders().containsKey( HEADER_AUTHORIZATION ) )
        {
            this.setAuthorization( new Authorization( this.getHeader( HEADER_AUTHORIZATION ) ) );
        }
    }

    @Override
    protected AbstractRequest newRequest(final String query)
    {
        Assert.notNull( query );

        AbstractRequest request = new Request();
        request.setPostRequest( this.getPostRequest() );
        request.getHeaders().putAll( this.getHeaders() );
        request.setSocket( this.getSocket() );
        request.setQuery( new Query( HttpMethod.GET, query ) );
        request.getMeta().putAll( this.getMeta() );
        request.setCookies( this.getCookies() );
        request.setProtocolType( this.getProtocolType() );
        request.setSession( this.getSession() );

        return request;
    }

    @Override
    public void requestSession()
    {
        if ( hasBeenRequested )
        {
            return;
        }
        hasBeenRequested = true;
        final Optional<ISession> session = ServerImplementation.getImplementation().getSessionManager()
                .getSession( this );
        if ( session.isPresent() )
        {
            setSession( session.get() );
            ServerImplementation.getImplementation().getSessionManager()
                    .setSessionLastActive( session.get().get( "id" ).toString() );
        } else
        {
            Logger.warn( "Could not initialize session!" );
        }
    }

    @Override
    public void dumpRequest()
    {
        final List<String> dump = new ArrayList<>();
        dump.add( "# Request Information " );
        dump.add( "├── Query: " + getQuery().getFullRequest() );
        dump.add( "├── POST Request: " );
        if ( getPostRequest() != null )
        {
            this.getPostRequest().get().forEach( (k, v) -> dump.add( "|\t├── Key: " + k + ", Value: " + v ) );
        } else
        {
            dump.add( "| None..." );
        }
        dump.add( "├── Headers: " );
        this.getHeaders().forEach( (k, v) -> dump.add( "|\t├── Key: " + k + ", Value: " + v ) );
        dump.add( "└── End" );
        dump.forEach( Logger::debug );
    }
}
