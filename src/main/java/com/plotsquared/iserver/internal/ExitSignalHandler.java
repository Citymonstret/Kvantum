package com.plotsquared.iserver.internal;

import com.plotsquared.iserver.core.Server;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public final class ExitSignalHandler implements SignalHandler
{

    @Override
    public void handle(Signal signal)
    {
        if ( signal.toString().equals( "SIGINT" ) )
        {
            Server.getInstance().stopServer();
        }
    }

}
