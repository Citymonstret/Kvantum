package com.intellectualsites.web;

import com.intellectualsites.web.core.Server;

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
        try {
            new Server(true).start();
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }
}
