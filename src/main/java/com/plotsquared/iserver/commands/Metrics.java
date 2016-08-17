package com.plotsquared.iserver.commands;

import com.plotsquared.iserver.core.Server;

/**
 * Prints {@link com.plotsquared.iserver.util.Metrics} information to
 * the logger
 */
public class Metrics extends Command
{

    @Override
    public void handle(String[] args)
    {
        Server.getInstance().getMetrics().logReport();
    }
}
