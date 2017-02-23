/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.internal;

import com.plotsquared.iserver.logging.LogWrapper;
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
