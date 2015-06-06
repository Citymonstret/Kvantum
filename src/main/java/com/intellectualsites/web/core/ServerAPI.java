package com.intellectualsites.web.core;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.View;
import com.intellectualsites.web.util.TimeUtil;
import ro.fortsoft.pf4j.Plugin;

/**
 * Created 2015-04-23 for IntellectualServer
 *
 * @author Citymonstret
 */
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

    private Server server;
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

    public void log(final Plugin plugin, final String message) {
        System.out.println(String.format("[%s][%s] %s", plugin.getWrapper().getDescriptor().getPluginId(), TimeUtil.getTimeStamp(TimeUtil.LogFormat), message)); // TODO Use custom logger
    }
}
