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
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.QueryException;
import xyz.kvantum.server.api.exceptions.RequestException;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.socket.SocketContext;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.CookieManager;
import xyz.kvantum.server.api.util.ProtocolType;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final public class Request extends AbstractRequest
{

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final Pattern PATTERN_QUERY = Pattern.compile(
            "(?<method>[A-Za-z]+) (?<resource>[/\\-A-Za-z0-9.?=&:@!%]*) " +
                    "(?<protocol>(?<prottype>[A-Za-z]+)/(?<protver>[A-Za-z0-9.]+))?"
    );
    private static final Pattern PATTERN_HEADER = Pattern.compile( "(?<key>[A-Za-z-_0-9]+)\\s*:\\s*(?<value>.*$)" );
    private boolean hasBeenRequested = false;

    public Request(final Collection<String> request, final SocketContext socket)
    {
        Assert.notNull( request, socket );

        this.setSocket( socket );

        boolean hasQuery = false;
        if ( !request.isEmpty() )
        {
            Matcher matcher;
            for ( final String part : request )
            {
                if ( !hasQuery )
                {
                    matcher = PATTERN_QUERY.matcher( part );
                    if ( matcher.matches() )
                    {
                        if ( CoreConfig.verbose )
                        {
                            ServerImplementation.getImplementation().log( "Query: " + matcher.group() );
                        }

                        this.getHeaders().put( "query", matcher.group() );

                        final Optional<HttpMethod> methodOptional = HttpMethod.getByName( matcher.group( "method" ) );
                        if ( !methodOptional.isPresent() )
                        {
                            throw new RequestException( "Unknown request method: " + matcher.group( "method" ),
                                    this );
                        }

                        if ( socket.isSSL() )
                        {
                            this.setProtocolType( ProtocolType.HTTPS );
                        } else
                        {
                            this.setProtocolType( ProtocolType.HTTP );
                        }

                        this.setQuery( new Query( methodOptional.get(), matcher.group( "resource" ) ) );
                        hasQuery = true;
                    }
                } else
                {
                    matcher = PATTERN_HEADER.matcher( part );
                    if ( matcher.matches() )
                    {
                        this.getHeaders().put( matcher.group( "key" ).toLowerCase(), matcher.group( "value" ) );
                    }
                }
            }
        }

        if ( !hasQuery )
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
                .getSession( this, this.outputStream );
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
}
