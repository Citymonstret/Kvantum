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
package xyz.kvantum.server.api.util;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.matching.Router;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.views.RequestHandler;
import xyz.kvantum.server.api.views.errors.View404;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A simple {@link Router} implementation,
 */
@SuppressWarnings("unused")
@Builder
final public class RequestManager extends Router
{

    private static Generator<AbstractRequest, RequestHandler> DEFAULT_404_GENERATOR =
            (request) -> View404.construct( request.getQuery().getFullRequest() );

    @Builder.Default
    private List<RequestHandler> views = new ArrayList<>();

    @Setter
    @Getter
    @NonNull
    @Builder.Default
    private Generator<AbstractRequest, RequestHandler> error404Generator = DEFAULT_404_GENERATOR;

    /**
     * Register a view to the request manager
     *
     * @param view The view to register
     */
    @Override
    public RequestHandler add(@NonNull final RequestHandler view)
    {
        final Optional<RequestHandler> illegalRequestHandler = LambdaUtil.getFirst( views, v -> v.toString()
                .equalsIgnoreCase( view.toString() ) );
        if ( illegalRequestHandler.isPresent() )
        {
            throw new IllegalArgumentException( "Duplicate view pattern!" );
        }
        views.add( view );
        return view;
    }

    /**
     * Try to find the request handler that matches the request
     *
     * @param request Incoming request
     * @return Matching request handler, or {@link #getError404Generator()} if none was found
     */
    @Override
    public RequestHandler match(final AbstractRequest request)
    {
        Assert.isValid( request );
        final Optional<RequestHandler> view = LambdaUtil.getFirst( views, request.matches );
        return view.orElseGet( () -> error404Generator.generate( request ) );
    }

    @Override
    public void dump(@NonNull final Kvantum server)
    {
        ( (IConsumer<RequestHandler>) view -> Message.REQUEST_HANDLER_DUMP.log( view.getClass().getSimpleName(),
                view.toString() ) ).foreach( views );
    }

    @Override
    public Collection<RequestHandler> getAll()
    {
        return ImmutableList.copyOf( this.views );
    }

    @Override
    public void remove(@NonNull final RequestHandler view)
    {
        if ( this.views.contains( view ) )
        {
            this.views.remove( view );
        }
    }

    @Override
    public void clear()
    {
        Message.CLEARED_VIEWS.log( CollectionUtil.clear( this.views ) );
    }

}
