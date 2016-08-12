package com.plotsquared.iserver.example;

import com.plotsquared.iserver.core.CoreConfig;
import com.plotsquared.iserver.core.DefaultLogWrapper;
import com.plotsquared.iserver.core.IntellectualServerMain;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.events.Event;
import com.plotsquared.iserver.events.EventCaller;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.views.decl.ViewMatcher;
import com.plotsquared.iserver.views.requesthandler.AuthenticationRequiredMiddleware;
import com.plotsquared.iserver.views.requesthandler.SimpleRequestHandler;
import com.plotsquared.iserver.views.staticviews.StaticViewManager;

import java.io.File;
import java.util.UUID;

public class Main
{

    public static void main(final String[] args)
    {
        final Main instance = new Main();

        CoreConfig.setPreConfigured( true ); // We want to configure everything via code
        CoreConfig.debug = true;
        CoreConfig.verbose = true;
        CoreConfig.gzip = true;
        CoreConfig.contentMd5 = true;
        CoreConfig.disableViews = true;

        final Server server = IntellectualServerMain.createServer( false, new File( "./testOtput" ), new
                LogHandler() );

        Assert.notNull( server );

        final SimpleRequestHandler indexHandler = new SimpleRequestHandler( "", (request, response) ->
                response.setContent( "<h1>Index</h1>" ) );
        final SimpleRequestHandler login = new SimpleRequestHandler( "login", (request, response) ->
                response.setContent( "<h1>Login!</h1>" ) );
        final SimpleRequestHandler authenticationRequired = new SimpleRequestHandler( "account", (request, response) ->
                response.setContent( "<h1>Nope!</h1>" ) );
        final SimpleRequestHandler hello = new SimpleRequestHandler( "hello/<name>", (request, response) ->
                response.setContent( "<h1>Hello {{request.name}}!</h1>" ) );

        // Just register these (can be inline as well :D)
        indexHandler.register();
        login.register();
        hello.register();

        // Use the ViewMatcher
        try
        {
            StaticViewManager.generate( instance );
        } catch ( Exception e )
        {
            e.printStackTrace();
        }

        // Using middleware
        authenticationRequired.getMiddlewareQueuePopulator().add( AuthenticationRequiredMiddleware.class );
        authenticationRequired.register();

        server.setEventCaller( new ExampleEventCaller() ); // Required!
        server.start();
    }

    @ViewMatcher( filter = "random/uuid/", cache = false, name = "randomUUID" )
    public Response randomUUID(final Request request) {
        return new Response().setContent( "<h1>" + UUID.randomUUID() + "</h1>" );
    }

    private static class ExampleEventCaller extends EventCaller
    {

        @Override
        public void callEvent(Event event)
        {
            // Just ignore the events!
        }
    }

    private static class LogHandler extends DefaultLogWrapper
    {

        @Override
        public void log(String prefix, String prefix1, String timeStamp, String message, String thread)
        {
            // Let's just override the provided prefix, for fun!
            super.log( "IntellectualServer", prefix1, timeStamp, message, thread );
        }
    }

}
