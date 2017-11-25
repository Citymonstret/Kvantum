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
package xyz.kvantum.server.api.logging;

import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.core.ServerImplementation;

@UtilityClass
public final class Logger
{

    public static void info(final String message, final Object... args)
    {
        ServerImplementation.getImplementation().log( message, LogModes.MODE_INFO, args );
    }

    public static void warn(final String message, final Object... args)
    {
        ServerImplementation.getImplementation().log( message, LogModes.MODE_WARNING, args );
    }

    public static void error(final String message, final Object... args)
    {
        ServerImplementation.getImplementation().log( message, LogModes.MODE_ERROR, args );
    }

    public static void debug(final String message, final Object... args)
    {
        ServerImplementation.getImplementation().log( message, LogModes.MODE_DEBUG, args );
    }

}
