package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.util.TimeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

final class LogStream extends PrintStream
{

    LogStream(final File logFolder) throws FileNotFoundException
    {
        super( new FileOutputStream( new File( logFolder, TimeUtil.getTimeStamp( TimeUtil.logFileFormat, new Date() )
                + ".txt" ) ) );
    }

}
