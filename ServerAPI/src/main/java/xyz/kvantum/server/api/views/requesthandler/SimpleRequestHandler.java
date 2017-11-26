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
package xyz.kvantum.server.api.views.requesthandler;

import lombok.Builder;
import lombok.NonNull;
import lombok.Setter;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.matching.ViewPattern;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.views.RequestHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

@SuppressWarnings({ "unused", "WeakerAccess" })
public class SimpleRequestHandler extends RequestHandler
{

    private static AtomicInteger identifier = new AtomicInteger( 0 );

    /**
     * An un-compiled {@link ViewPattern}
     */
    @NonNull
    private final String pattern;

    /**
     * The generator that will be used to serve
     * the response
     */
    @NonNull
    private final BiConsumer<AbstractRequest, Response> generator;

    /**
     * Whether or not the request should be forced over HTTPS
     */
    private boolean forceHTTPS = false;

    /**
     * The internal (unique) identifier for this request handler
     */
    @Setter
    private String internalName = "simpleRequestHandler::" + identifier.getAndIncrement();

    /**
     * The HTTP method that this request handler will accept
     */
    @NonNull
    private HttpMethod httpMethod = HttpMethod.ALL;

    private ViewPattern compiledPattern;

    protected SimpleRequestHandler(final String pattern, final BiConsumer<AbstractRequest, Response> generator)
    {
        this( pattern, generator, false, HttpMethod.ALL );
    }

    @Builder
    protected SimpleRequestHandler(final String pattern,
                                   final BiConsumer<AbstractRequest, Response> generator,
                                   final boolean forceHTTPS,
                                   final HttpMethod httpMethod)
    {
        this.pattern = pattern;
        this.generator = generator;
        this.forceHTTPS = forceHTTPS;
        if ( httpMethod == null )
        {
            this.httpMethod = HttpMethod.ALL;
        } else
        {
            this.httpMethod = httpMethod;
        }
    }

    public final SimpleRequestHandler addToRouter(final Router router)
    {
        return (SimpleRequestHandler) router.add( this );
    }

    protected ViewPattern getPattern()
    {
        if ( compiledPattern == null )
        {
            compiledPattern = new ViewPattern( pattern );
        }
        return compiledPattern;
    }

    @Override
    public String toString()
    {
        return this.pattern;
    }

    @Override
    public boolean matches(final AbstractRequest request)
    {
        final HttpMethod requestMethod = request.getQuery().getMethod();
        if ( this.httpMethod != HttpMethod.ALL && this.httpMethod != requestMethod )
        {
            return false;
        }
        final Map<String, String> map = getPattern().matches( request.getQuery().getFullRequest() );
        if ( map != null )
        {
            request.addMeta( "variables", map );
        }
        return map != null;
    }

    @Override
    public final Response generate(final AbstractRequest r)
    {
        final Response response = new Response( this );
        generator.accept( r, response );
        return response;
    }

    @Override
    public String getName()
    {
        return this.internalName;
    }

    @Override
    public final boolean forceHTTPS()
    {
        return this.forceHTTPS;
    }
}
