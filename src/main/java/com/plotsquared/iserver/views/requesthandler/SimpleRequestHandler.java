package com.plotsquared.iserver.views.requesthandler;

import com.plotsquared.iserver.core.CoreConfig;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.matching.ViewPattern;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.Final;
import com.plotsquared.iserver.views.RequestHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class SimpleRequestHandler extends RequestHandler
{

    private static AtomicInteger identifier = new AtomicInteger( 0 );

    private final String pattern;
    private final BiConsumer<Request, Response> generator;
    private String internalName = "simpleRequestHandler::" + identifier.getAndIncrement();
    private ViewPattern compiledPattern;

    {
        if ( CoreConfig.debug )
        {
            Server.getInstance().log( "Adding DebugMiddleware to SimpleRequestHandler" );
            this.middlewareQueuePopulator.add( DebugMiddleware.class );
        }
    }

    public SimpleRequestHandler(String pattern, BiConsumer<Request, Response> generator)
    {
        this.pattern = pattern;
        this.generator = generator;
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
    public boolean matches(final Request request)
    {
        if ( CoreConfig.debug )
        {
            request.addMeta( "zmetakey", UUID.randomUUID().toString() );
        }
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
        Server.getInstance().getRequestManager().add( this );
    }

}
