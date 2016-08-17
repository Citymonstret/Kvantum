package com.plotsquared.iserver.util;

import com.plotsquared.iserver.core.Server;

import java.io.PrintStream;

final public class LogPrintStream extends PrintStream
{

    public LogPrintStream()
    {
        super( new StringOutputStream( string -> Server.getInstance().log( string ) ), true );
    }

}
