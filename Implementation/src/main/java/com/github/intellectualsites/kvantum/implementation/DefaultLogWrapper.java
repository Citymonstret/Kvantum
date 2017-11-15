/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.logging.LogContext;
import com.github.intellectualsites.kvantum.api.logging.LogWrapper;
import com.github.intellectualsites.kvantum.api.util.Assert;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StrSubstitutor;

/**
 * The default log handler. UsesAnsi.FColor for colored output
 */
@SuppressWarnings( "WeakerAccess" )
@AllArgsConstructor
public class DefaultLogWrapper implements LogWrapper
{

    @Setter
    private String format;

    public DefaultLogWrapper()
    {
        this( CoreConfig.Logging.logFormat );
    }

    @Override
    public void log(final LogContext logContext)
    {
        Assert.notNull( logContext );
        final String replacedMessage = StrSubstitutor.replace( format, logContext.toMap() );
        if ( ServerImplementation.hasImplementation() )
        {
            ( (Server) ServerImplementation.getImplementation() ).logStream.println( replacedMessage );
        }
        System.out.println( replacedMessage );
    }

    @Override
    public void log(final String s)
    {
        Assert.notNull( s );
        System.out.println( s );
        ( (Server) ServerImplementation.getImplementation() ).logStream.println( s );
    }

}
