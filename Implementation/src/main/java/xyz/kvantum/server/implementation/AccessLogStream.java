package xyz.kvantum.server.implementation;

import de.jungblut.datastructure.AsyncBufferedOutputStream;
import lombok.NonNull;
import pw.stamina.causam.scan.method.model.Subscriber;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.response.FinalizedResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

final class AccessLogStream extends PrintStream
{

    AccessLogStream(final File logFolder) throws FileNotFoundException
    {
        super( new AsyncBufferedOutputStream( new FileOutputStream( new File( logFolder, "access.log" ), true ) ) );
    }

    @Subscriber
    private void onRequestFinish(@NonNull final FinalizedResponse response)
    {
        Logger.info( "Logging {}", response.toLogString() );
        this.println( response.toLogString() );
        this.flush();
    }

}
