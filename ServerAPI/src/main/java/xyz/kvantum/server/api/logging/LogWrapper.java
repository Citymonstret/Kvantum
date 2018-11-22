/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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

/**
 * Wrapper meant to hide the logging implementation details
 */
public interface LogWrapper
{

	/**
	 * Log a constructed context
	 *
	 * @param logContext Context to log
	 */
	void log(LogContext logContext);

	/**
	 * Log a message
	 *
	 * @param s Log message
	 */
	void log(String s);

	/**
	 * Send a line break
	 */
	void breakLine();

	/**
	 * Log an empty string
	 */
	default void log()
	{
		log( "" );
	}

	default void log(final LogEntryFormatter formatter, final String s)
	{
		if ( formatter != null )
		{
			log( formatter.process( s ) );
		}
	}

	interface LogEntryFormatter
	{

		String process(String in);
	}
}
