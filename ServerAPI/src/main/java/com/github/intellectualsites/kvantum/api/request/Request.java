package com.github.intellectualsites.kvantum.api.request;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.exceptions.QueryException;
import com.github.intellectualsites.kvantum.api.exceptions.RequestException;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.session.ISession;
import com.github.intellectualsites.kvantum.api.socket.SocketContext;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.CookieManager;
import com.github.intellectualsites.kvantum.api.util.ProtocolType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
    protected AbstractRequest newRequest(String query)
    {
        Assert.notEmpty( query );

        AbstractRequest request = new Request();
        request.getHeaders().putAll( this.getHeaders() );
        request.setSocket( this.getSocket() );
        request.setQuery( new Query( HttpMethod.GET, query ) );
        request.getMeta().putAll( this.getMeta() );
        request.setCookies( this.getCookies() );
        request.setProtocolType( this.getProtocolType() );

        return request;
    }

    @Override
    public void requestSession()
    {
        if ( this.getSession() != null )
        {
            return;
        }
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
