package com.intellectualsites.web.views;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.syntax.ProviderFactory;

public abstract class RequestHandler {

    abstract public boolean matches(final Request request);

    abstract public Response generate(final Request r);

    /**
     * Get the view specific factory (if it exists)
     *
     * @param r Request IN
     * @return Null by default, or the ProviderFactory (if set by the view)
     */
    public ProviderFactory getFactory(final Request r) {
        return null;
    }

    abstract public String getName();

}
