package com.intellectualsites.web.commands;

import com.intellectualsites.web.core.Server;

/**
 * Created 2015-07-08 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Stop extends Command {

    @Override
    public void handle(String[] args) {
        Server.getInstance().stop();
    }

}
