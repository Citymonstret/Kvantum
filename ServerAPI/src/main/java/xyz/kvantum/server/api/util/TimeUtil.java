/*
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.util;

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
