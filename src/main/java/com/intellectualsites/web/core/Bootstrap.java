package com.intellectualsites.web.core;

/**
 * This is a booster, I.E, it's the strap on a boot.
 */
public class Bootstrap {

    /**
     * Launcher method
     * @param args arguments
     */
    public static void main(String[] args) {
        startServer(true);
    }

    /**
     * Start a server, and get the instance
     *
     * @param standalone Should it run as a standalone application, or be integrated
     * @return the started server | null
     */
    public static Server startServer(boolean standalone) {
        Server server = null;
        try {
            server = new Server(standalone);
            server.start();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        return server;
    }
}
