package com.plotsquared.iserver.util;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.logging.LogModes;

public final class Logger
{

    public static void info(String message, final Object ... args)
    {
        Server.getInstance().log( message, LogModes.MODE_INFO, args );
    }

    public static void warn(String message, final Object ... args)
    {
        Server.getInstance().log( message, LogModes.MODE_WARNING, args );
    }

    public static void error(String message, final Object ... args)
    {
        Server.getInstance().log( message, LogModes.MODE_ERROR, args );
    }

    public static void debug(String message, final Object ... args)
    {
        Server.getInstance().log( message, LogModes.MODE_DEBUG, args );
    }

}
