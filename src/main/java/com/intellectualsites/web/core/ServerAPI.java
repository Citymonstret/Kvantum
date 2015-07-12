package com.intellectualsites.web.core;

import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.views.View;

/**
 * Server API
 *
 * @deprecated
 */
@SuppressWarnings({"unused","deprecation"})
public class ServerAPI {

    private static ServerAPI instance;

    /**
     * Get the server api instance
     *
     * @return WebServer API instance
     */
    public static ServerAPI instance() {
        return instance;
    }

    protected static void setInstance(final Server server) {
        instance = new ServerAPI(server);
    }

    private final Server server;
    protected ServerAPI(final Server server) {
        this.server = server;
    }

    public void addView(final View view) {
        server.viewManager.add(view);
    }

    public void removeView(final View view) {
        server.viewManager.remove(view);
    }

    public void addProviderFactory(final ProviderFactory factory) {
        server.providers.add(factory);
    }
}
