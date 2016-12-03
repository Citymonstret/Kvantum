package com.plotsquared.iserver.internal;

import com.plotsquared.iserver.object.LogWrapper;
import com.plotsquared.iserver.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Extremely hacky solution that enables file logging for exceptions
 *
 * @author Citymonstret
 */
public final class ErrorOutputStream extends ByteArrayOutputStream
{

    private final LogWrapper logWrapper;

    public ErrorOutputStream(final LogWrapper logWrapper)
    {
        this.logWrapper = Assert.notNull( logWrapper );
    }

    @Override
    public void flush() throws IOException
    {
        String message = new String( toByteArray(), StandardCharsets.UTF_8 );
        if ( message.endsWith( System.lineSeparator() ) )
        {
            message = message.substring( 0, message.length() - System.lineSeparator().length() );
        }
        if ( !message.isEmpty() )
        {
            logWrapper.log( new String( toByteArray(), StandardCharsets.UTF_8 ) );
        }
        super.reset();
    }
}
