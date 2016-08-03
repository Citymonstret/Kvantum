//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualsites.web.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A time utility class
 *
 * @author Citymonstret
 */
public class TimeUtil {

    private final static SimpleDateFormat HTTPFormat;
    private final static SimpleDateFormat LogFormat;
    public final static SimpleDateFormat LogFileFormat;
    static {
        HTTPFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss 'GMT'", Locale.US);
        LogFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        LogFileFormat = new SimpleDateFormat("dd MMM yyyy kk-mm-ss", Locale.US);
    }

    public static String getTimeStamp() {
        return getTimeStamp(LogFormat);
    }

    /**
     * Returns a string with date formatted with
     * HTTP-date format as defined by RFC 7231
     *
     * @return RFC 7231 formatted timestamp
     */
    public static String getHTTPTimeStamp() {
        return getTimeStamp(HTTPFormat);
    }

    public static String getTimeStamp(final String format) {
        return getTimeStamp(new SimpleDateFormat(format));
    }

    public static String getTimeStamp(final SimpleDateFormat format) {
        return format.format(new Date());
    }
}
