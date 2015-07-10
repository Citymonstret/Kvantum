package com.intellectualsites.web.core;

import com.intellectualsites.web.object.LogWrapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a booster, I.E, it's the strap on a boot.
 */
public class Bootstrap {

    private static Map<String, String> getOptions(final String[] args) {
        Map<String, String> out = new HashMap<>();
        for (final String arg : args) {
            String[] parts = arg.split("=");
            if (parts.length < 2) {
                out.put(parts[0].toLowerCase(), null);
            } else {
                out.put(parts[0].toLowerCase(), parts[1]);
            }
        }
        return out;
    }

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
        Map<String, String> options = getOptions(args);
        File file;
        if (options.containsKey("folder")) {
            // folder=./this/new/path
            // folder=/web/intellectualserver/
            // and etc.
            file = new File(options.get("folder"));
        } else {
            file = new File("./");
        }
        startServer(true, file);
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
