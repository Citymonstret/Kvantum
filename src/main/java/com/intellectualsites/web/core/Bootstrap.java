package com.intellectualsites.web.core;

import com.intellectualsites.web.object.LogWrapper;

import java.io.File;

/**
 * This is a booster, I.E, it's the strap on a boot.
 */
public class Bootstrap {

    /**
     * Launcher method
     * @param args arguments
     */
    public static void main(String[] args) {
        startServer(true, new File("./"), new LogWrapper() {
            @Override
            public void log(String s) {
                System.out.print(s);
            }
        });
    }

    /**
     * Start a server, and get the instance
     *
     * @param standalone Should it run as a standalone application, or be integrated
     * @return the started server | null
     */
    public static Server startServer(boolean standalone, File coreFolder, LogWrapper wrapper) {
        Server server = null;
        try {
            server = new Server(standalone, coreFolder, wrapper);
            server.start();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        return server;
    }
}
