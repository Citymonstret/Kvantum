package com.intellectualsites.web.core;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Bootstrap {

    /**
     * Launcher method
     * @param args arguments
     */
    public static void main(String[] args) {
        startServer(true);
    }

    public static void startServer(boolean standalone) {
        try {
            new Server(standalone).start();
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }
}
