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
package com.github.intellectualsites.kvantum.api.views.requesthandler;

import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.matching.Router;
import com.github.intellectualsites.kvantum.api.matching.ViewPattern;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.response.Response;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class SimpleRequestHandler extends RequestHandler
{

    private static AtomicInteger identifier = new AtomicInteger( 0 );

    private final String pattern;
    private final BiConsumer<AbstractRequest, Response> generator;
    private final boolean forceHTTPS;
    private String internalName = "simpleRequestHandler::" + identifier.getAndIncrement();
    private ViewPattern compiledPattern;

    protected SimpleRequestHandler(String pattern, BiConsumer<AbstractRequest, Response> generator)
    {
        this( pattern, generator, false );
    }

    protected SimpleRequestHandler(String pattern, BiConsumer<AbstractRequest, Response> generator, boolean forceHTTPS)
    {
        this.pattern = pattern;
        this.generator = generator;
        this.forceHTTPS = forceHTTPS;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public SimpleRequestHandler addToRouter(final Router router)
    {
        return (SimpleRequestHandler) router.add( this );
    }

    public void setInternalName(String internalName)
    {
        this.internalName = internalName;
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
    public boolean forceHTTPS()
    {
        return this.forceHTTPS;
    }

    final public void register()
    {
        ServerImplementation.getImplementation().getRouter().add( this );
    }

    public static final class Builder
    {

        private String pattern;
        private BiConsumer<AbstractRequest, Response> generator;

        private Builder()
        {
        }

        public Builder setPattern(final String pattern)
        {
            this.pattern = pattern;
            return this;
        }

        public Builder setGenerator(final BiConsumer<AbstractRequest, Response> generator)
        {
            this.generator = generator;
            return this;
        }

        public SimpleRequestHandler build()
        {
            return new SimpleRequestHandler( pattern, generator );
        }
    }

}
