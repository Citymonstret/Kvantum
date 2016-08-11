package com.plotsquared.iserver.views;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.Response;
import com.plotsquared.iserver.object.syntax.ProviderFactory;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.views.requesthandler.MiddlewareQueue;
import com.plotsquared.iserver.views.requesthandler.MiddlewareQueuePopulator;

/**
 * A handler which uses an incoming
 * request to generate a response
 */
public abstract class RequestHandler {

    protected final MiddlewareQueuePopulator middlewareQueuePopulator = new MiddlewareQueuePopulator();

    public MiddlewareQueuePopulator getMiddlewareQueuePopulator() {
        return middlewareQueuePopulator;
    }

    /**
     * Used to check if a request is to be served
     * by this RequestHandler
     *
     * @param request Incoming request
     * @return True if the request can be served by this handler
     * False if not
     */
    abstract public boolean matches(final Request request);

    final public Response handle(final Request request) {
        Assert.isValid(request);

        final MiddlewareQueue middlewareQueue = middlewareQueuePopulator.generateQueue();
        middlewareQueue.handle(request);
        if (!middlewareQueue.finished()) {
            Server.getInstance().log("Skipping request as a middleware broke the chain!");
            return null;
        }
        return generate(request);
    }

    /**
     * Generate a response for the incoming request
     *
     * @param request The incoming request
     * @return The generated response
     */
    abstract public Response generate(final Request request);

    /**
     * Get the view specific factory (if it exists)
     *
     * @param r Request IN
     * @return Null by default, or the ProviderFactory (if set by the view)
     */
    public ProviderFactory getFactory(final Request r) {
        return null;
    }

    /**
     * Get the unique internal name of the handler
     *
     * @return Handler name
     */
    abstract public String getName();

}
