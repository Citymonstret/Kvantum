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
package com.github.intellectualsites.kvantum.api.util;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class providing methods for dealing with timestamps
 */
@SuppressWarnings( "WeakerAccess" )
@UtilityClass
public class TimeUtil
{

    public final static SimpleDateFormat logFileFormat;
    public final static SimpleDateFormat httpFormat;
    public final static SimpleDateFormat logFormat;

    static
    {
        httpFormat = new SimpleDateFormat( "EEE, dd MMM yyyy kk:mm:ss 'GMT'", Locale.US );
        logFormat = new SimpleDateFormat( "HH:mm:ss", Locale.US );
        logFileFormat = new SimpleDateFormat( "dd MMM yyyy kk-mm-ss", Locale.US );
    }

    /**
     * Get a log formatted timestamp
     * @return log formatted timestamp
     */
    public static String getTimeStamp()
    {
        return getTimeStamp( logFormat, new Date() );
    }

    /**
     * Returns a string with date formatted with
     * HTTP-date format as defined by RFC 7231
     * @param date Date from which the timestamp is generated
     * @return RFC 7231 formatted timestamp
     */
    public static String getHTTPTimeStamp(final Date date)
    {
        return getTimeStamp( httpFormat, date );
    }

    /**
     * Returns a string with current timestamp formatted with
     * HTTP-date format as defined by RFC 7231
     * @return RFC 7231 formatted timestamp
     */
    public static String getHTTPTimeStamp()
    {
        return getTimeStamp( httpFormat, new Date() );
    }

    public static String getTimeStamp(final SimpleDateFormat format, final Date date)
    {
        return format.format( date );
    }
}
