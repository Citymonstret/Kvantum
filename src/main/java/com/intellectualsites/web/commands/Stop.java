package com.intellectualsites.web.commands;

import com.intellectualsites.web.core.Server;

public class Stop extends Command {

    @Override
    public void handle(String[] args) {
        Server.getInstance().stopServer();
    }

}
