/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.request;

import com.plotsquared.iserver.api.config.CoreConfig;
import com.plotsquared.iserver.api.config.Message;
import com.plotsquared.iserver.api.core.ServerImplementation;
import com.plotsquared.iserver.api.session.ISession;
import com.plotsquared.iserver.api.util.*;
import com.plotsquared.iserver.api.views.CookieManager;
import com.plotsquared.iserver.api.views.RequestHandler;

import javax.net.ssl.SSLSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

/**
 * The HTTP Request Class
 * <p>
 * This is generated when a client
 * connects to the web server, and
 * contains the information needed
 * for the server to generate a
 * proper response. This is what
 * everything is based around!
 *
 * @author Citymonstret
 */
@SuppressWarnings("unused")
final public class Request implements ProviderFactory<Request>, VariableProvider, Validatable, RequestChild
{

    public static final String ALTERNATE_OUTCOME = "alternateOutcome";
    public static final String INTERNAL_REDIRECT = "internalRedirect";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    final public Predicate<RequestHandler> matches = view -> view.matches( this );
    public Map<String, String> postponedCookies = new HashMap<>();
    private ProtocolType protocolType;
    private Map<String, Object> meta;
    private Map<String, String> headers;
    private Cookie[] cookies;
    private Query query;
    private PostRequest postRequest;
    private Socket socket;
    private ISession session;
    private boolean valid = true;
    private Authorization authorization;

    private Request()
    {
    }

    /**
     * The request constructor
     *
     * @param request Request (from the client)
     * @param socket  The socket that sent the request
     * @throws RuntimeException if the request doesn't contain a query
     */
    public Request(final Collection<String> request, final Socket socket)
    {
        Assert.notNull( request, socket );

        protocolType = ( socket instanceof SSLSocket ) ? ProtocolType.HTTPS : ProtocolType.HTTP;

        this.socket = socket;
        this.headers = new HashMap<>();

        // Read the request line per line
        for ( final String part : request )
        {
            final String[] subParts = part.split( ":" );
            if ( subParts.length < 2 )
            {
                if ( headers.containsKey( "query" ) )
                {
                    // This fixes issues with Nginx and proxy_pass
                    continue;
                }
                if ( CoreConfig.verbose )
                {
                    ServerImplementation.getImplementation().log( "Query: " + subParts[ 0 ] );
                }
                headers.put( "query", subParts[ 0 ] );
            } else
            {
                headers.put( subParts[ 0 ], subParts[ 1 ] );
            }
        }
        if ( !this.headers.containsKey( "query" ) )
        {
            throw new RuntimeException( "Couldn't find query header..." );
        }
        this.getResourceRequest();
        this.cookies = CookieManager.getCookies( this );
        this.meta = new HashMap<>();
        if ( this.headers.containsKey( HEADER_AUTHORIZATION ) )
        {
            this.authorization = new Authorization( this.headers.get( HEADER_AUTHORIZATION ) );
        }
    }

    @Override
    public Request getParent()
    {
        return this;
    }

    public void removeMeta(String metaKey)
    {
        this.meta.remove( Assert.notEmpty( metaKey ) );
    }

    public Optional<Authorization> getAuthorization()
    {
        return Optional.ofNullable( authorization );
    }

    @Override
    public Optional<Request> get(Request r)
    {
        return Optional.of( this );
    }

    @Override
    public String providerName()
    {
        return null;
    }

    @Override
    public boolean contains(String variable)
    {
        Assert.notNull( variable );

        return getVariables().containsKey( variable );
    }

    @Override
    public Object get(String variable)
    {
        Assert.notNull( variable );

        return getVariables().get( variable );
    }

    @Override
    public Map<String, Object> getAll()
    {
        return MapUtil.convertMap( getVariables(), (s) -> s );
    }

    public void useAlternateOutcome(final String identifier)
    {
        Assert.notEmpty( identifier );

        this.addMeta( ALTERNATE_OUTCOME, identifier );
    }

    @Override
    public boolean isValid()
    {
        return this.valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    /**
     * Get the PostRequest
     *
     * @return PostRequest if exists, null if not
     */
    public PostRequest getPostRequest()
    {
        if ( this.postRequest == null )
        {
            this.postRequest = new PostRequest( this, "&" );
        }
        return this.postRequest;
    }

    /**
     * The post request is basically... a POST request.
     *
     * @param postRequest The post request
     */
    public void setPostRequest(final PostRequest postRequest)
    {
        Assert.notNull( postRequest );

        this.postRequest = postRequest;
    }

    private Request newRequest(String query)
    {
        Assert.notEmpty( query );

        Request request = new Request();
        request.headers = new HashMap<>( headers );
        request.socket = this.socket;
        request.query = new Query( HttpMethod.GET, query );
        request.meta = new HashMap<>( meta );
        request.cookies = cookies;
        request.protocolType = protocolType;
        return request;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    /**
     * Get all request cookies
     *
     * @return Request cookies
     */
    public Cookie[] getCookies()
    {
        return this.cookies;
    }

    public ProtocolType getProtocolType()
    {
        return this.protocolType;
    }

    /**
     * Get a request header. These
     * are sent by the client, and
     * are not to be confused with the
     * response headers.
     *
     * @param name Header Name
     * @return The header value, if the header exists. Otherwise an empty string will be returned.
     */
    public String getHeader(final String name)
    {
        Assert.notNull( name );

        if ( this.headers.containsKey( name ) )
        {
            return this.headers.get( name );
        }
        return "";
    }

    public Query getResourceRequest()
    {
        if ( this.query != null )
        {
            return getQuery();
        }
        final String[] parts = getHeader( "query" ).split( " " );
        if ( parts.length < 3 )
        {
            this.query = new Query( HttpMethod.GET, "/" );
        } else
        {
            final Optional<HttpMethod> methodOptional = HttpMethod.getByName( parts[ 0 ] );
            if ( !methodOptional.isPresent() )
            {
                throw new RuntimeException( "Unknown request method: " + parts[ 0 ] );
            }
            this.query = new Query( methodOptional.get(), parts[ 1 ] );
        }
        return this.query;
    }

    /**
     * Get the built query
     *
     * @return Compiled query
     */
    public Query getQuery()
    {
        return this.query;
    }

    /**
     * Build a string for logging
     *
     * @return Compiled string
     */
    public String buildLog()
    {
        String msg = Message.REQUEST_LOG.toString();
        for ( final Object a : new String[]{ socket.getRemoteSocketAddress().toString(), getHeader( "User-Agent" ),
                getHeader( "query" ), getHeader( "Host" ), this.query.buildLog(), postRequest != null ? postRequest
                .buildLog() : "" } )
        {
            msg = msg.replaceFirst( "%s", a.toString() );
        }
        return msg;
    }

    /**
     * Add a meta value, which can
     * be used to share an object
     * throughout the lifespan of
     * the request.
     *
     * @param name Key (which will be used to get the meta value)
     * @param var  Value (Any object will do)
     * @see #getMeta(String) To get the value
     */
    public void addMeta(final String name, final Object var)
    {
        Assert.notNull( name, var );

        meta.put( name, var );
    }

    final public void internalRedirect(final String url)
    {
        Assert.notNull( url );

        this.addMeta( INTERNAL_REDIRECT, newRequest( url ) );
        Message.INTERNAL_REDIRECT.log( url );
    }

    /**
     * Get a meta value
     *
     * @param name The key
     * @return Meta value if exists, else null
     * @see #addMeta(String, Object) To set a meta value
     */
    public Object getMeta(final String name)
    {
        Assert.notNull( name );

        if ( !meta.containsKey( name ) )
        {
            return null;
        }
        return meta.get( name );
    }

    @Final
    final public Map<String, String> getVariables()
    {
        return (Map<String, String>) getMeta( "variables" );
    }

    /**
     * Get the internal session
     *
     * @return true|null
     */
    public ISession getSession()
    {
        return this.session;
    }

    /**
     * Set the internal session
     *
     * @param session Session
     */
    public void setSession(final ISession session)
    {
        Assert.notNull( session );

        this.session = session;
    }

    @Override
    public String toString()
    {
        return this.socket.getInetAddress().getHostName();
    }

    public boolean hasMeta(final String key)
    {
        Assert.notEmpty( key );

        return this.meta.containsKey( key );
    }

    public Map<String, Object> getAllMeta()
    {
        return new HashMap<>( this.meta );
    }

    /**
     * The query, for example:
     * "http://localhost/query?example=this"
     */
    public static class Query
    {

        private final HttpMethod method;
        private final String resource;
        private final Map<String, String> parameters = new HashMap<>();

        /**
         * The query constructor
         *
         * @param method   Request Method
         * @param resource The requested resource
         */
        Query(HttpMethod method, String resource)
        {
            Assert.notNull( method, resource );

            this.method = method;
            if ( resource.contains( "?" ) )
            {
                final String[] parts = resource.split( "\\?" );
                final String[] subParts = parts[ 1 ].split( "&" );
                resource = parts[ 0 ];
                for ( final String part : subParts )
                {
                    final String[] subSubParts = part.split( "=" );
                    this.parameters.put( subSubParts[ 0 ], subSubParts[ 1 ] );
                }
            }
            this.resource = resource;
        }

        public HttpMethod getMethod()
        {
            return this.method;
        }

        public String getResource()
        {
            return this.resource;
        }

        public Map<String, String> getParameters()
        {
            return this.parameters;
        }

        /**
         * Build a logging string... for logging?
         *
         * @return compiled string
         */
        String buildLog()
        {
            return "Query: [Method: " + method.toString() + " | Resource: " + resource + "]";
        }

        public String getFullRequest()
        {
            final String parameters = StringUtil.join( getParameters(), "=", "&" );
            return parameters.isEmpty() ? resource : resource + "?" + parameters;
        }

    }

    /**
     * Used to handle HTTP authentication
     */
    @SuppressWarnings("unused")
    public class Authorization
    {

        private final String mechanism, username, password;

        private Authorization(final String input)
        {
            final String[] parts = input.split( "\\s" );
            this.mechanism = parts[ 1 ];
            final String[] auth = new String( Base64.getDecoder().decode( parts[ 2 ] ), StandardCharsets.UTF_8 )
                    .split( ":" );
            if ( auth.length < 2 )
            {
                this.username = null;
                this.password = null;
            } else
            {
                this.username = auth[ 0 ];
                this.password = auth[ 1 ];
            }
        }

        public boolean isValid()
        {
            return this.mechanism != null && this.username != null && this.password != null;
        }

        public String getMechanism()
        {
            return mechanism;
        }

        public String getUsername()
        {
            return username;
        }

        public String getPassword()
        {
            return password;
        }

    }

}
