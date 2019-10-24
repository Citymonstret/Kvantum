/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class providing methods for dealing with timestamps
 */
@SuppressWarnings("WeakerAccess") @UtilityClass public final class TimeUtil {

    public final static SimpleDateFormat logFileFormat;
    public final static SimpleDateFormat httpFormat;
    public static final SimpleDateFormat accessLogFormat;

    static {
        httpFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss 'GMT'", Locale.ENGLISH);
        logFileFormat = new SimpleDateFormat("dd MMM yyyy kk-mm-ss", Locale.ENGLISH);
        accessLogFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
    }

    /**
     * Returns a string with date formatted with HTTP-date format as defined by RFC 7231
     *
     * @param date Date from which the timestamp is generated
     * @return RFC 7231 formatted timestamp
     */
    public static String getHTTPTimeStamp(final Date date) {
        return getTimeStamp(httpFormat, date);
    }

    public static String getAccessLogTimeStamp(final long time) {
        return getTimeStamp(accessLogFormat, new Date(time));
    }

    /**
     * Returns a string with current timestamp formatted with HTTP-date format as defined by RFC 7231
     *
     * @return RFC 7231 formatted timestamp
     */
    public static String getHTTPTimeStamp() {
        return getTimeStamp(httpFormat, new Date());
    }

    public static String getTimeStamp(final SimpleDateFormat format, final Date date) {
        return format.format(date);
    }
}
