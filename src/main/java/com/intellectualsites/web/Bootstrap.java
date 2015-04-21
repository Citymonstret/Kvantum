package com.intellectualsites.web;

import com.intellectualsites.web.core.Server;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Bootstrap {

    /**
     * Boostrap method
     * @param args arguments
     */
    public static void main(String[] args) {
        // TODO Load configuration options
        try {
            new Server().start();
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }
}
