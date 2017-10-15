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
package com.github.intellectualsites.iserver.api.views.requesthandler;

import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.matching.Router;
import com.github.intellectualsites.iserver.api.matching.ViewPattern;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Response;
import com.github.intellectualsites.iserver.api.util.Final;
import com.github.intellectualsites.iserver.api.views.RequestHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class SimpleRequestHandler extends RequestHandler
{

    private static AtomicInteger identifier = new AtomicInteger( 0 );
    private final String pattern;
    private final BiConsumer<Request, Response> generator;
    private String internalName = "simpleRequestHandler::" + identifier.getAndIncrement();
    private ViewPattern compiledPattern;

    protected SimpleRequestHandler(String pattern, BiConsumer<Request, Response> generator)
    {
        this.pattern = pattern;
        this.generator = generator;
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
    public boolean matches(final Request request)
    {
        final Map<String, String> map = getPattern().matches( request.getQuery().getFullRequest() );
        if ( map != null )
        {
            request.addMeta( "variables", map );
        }
        return map != null;
    }

    @Override
    public final Response generate(final Request r)
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

    @Final
    final public void register()
    {
        ServerImplementation.getImplementation().getRouter().add( this );
    }

    public static final class Builder
    {

        private String pattern;
        private BiConsumer<Request, Response> generator;

        private Builder()
        {
        }

        public Builder setPattern(final String pattern)
        {
            this.pattern = pattern;
            return this;
        }

        public Builder setGenerator(final BiConsumer<Request, Response> generator)
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
