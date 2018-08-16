/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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

/**
 * Static utility class for Logging purposes
 */
@UtilityClass public final class Logger
{

	/**
	 * Log an informational message to the server implementation logger <p> Replaces string arguments using the pattern
	 * {num} from an array of objects, starting from index 0, as such: 0 &le; num &lt; args.length. If num &ge;
	 * args.length, then the pattern will be replaced by an empty string. An argument can also be passed as "{}" or
	 * "{}", in which case the number will be implied.
	 *
	 * @param message message to be logged
	 * @param args Replacements
	 */
	public static void info(final String message, final Object... args)
	{
		ServerImplementation.getImplementation().log( message, LogModes.MODE_INFO, args );
	}

	/**
	 * Log a warning message to the server implementation logger <p> Replaces string arguments using the pattern {num}
	 * from an array of objects, starting from index 0, as such: 0 &le; num &lt; args.length. If num &ge; args.length,
	 * then the pattern will be replaced by an empty string. An argument can also be passed as "{}" or "{}", in which
	 * case the number will be implied.
	 *
	 * @param message message to be logged
	 * @param args Replacements
	 */
	public static void warn(final String message, final Object... args)
	{
		ServerImplementation.getImplementation().log( message, LogModes.MODE_WARNING, args );
	}

	/**
	 * Log an error message to the server implementation logger
	 *
	 * Replaces string arguments using the pattern {num} from an array of objects, starting from index 0, as such: 0
	 * &le; num &lt; args.length. If num &ge; args.length, then the pattern will be replaced by an empty string. An
	 * argument can also be passed as "{}" or "{}", in which case the number will be implied.
	 *
	 * @param message message to be logged
	 * @param args Replacements
	 */
	public static void error(final String message, final Object... args)
	{
		ServerImplementation.getImplementation().log( message, LogModes.MODE_ERROR, args );
	}

	/**
	 * Log a debug message to the server implementation logger
	 *
	 * Replaces string arguments using the pattern {num} from an array of objects, starting from index 0, as such: 0
	 * &le; num &lt; args.length. If num &ge; args.length, then the pattern will be replaced by an empty string. An
	 * argument can also be passed as "{}" or "{}", in which case the number will be implied.
	 *
	 * @param message message to be logged
	 * @param args Replacements
	 */
	public static void debug(final String message, final Object... args)
	{
		ServerImplementation.getImplementation().log( message, LogModes.MODE_DEBUG, args );
	}

}
