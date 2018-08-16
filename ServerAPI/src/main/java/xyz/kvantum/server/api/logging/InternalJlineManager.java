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

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class InternalJlineManager
{

	private static InternalJlineManager instance;
	@Getter private final Terminal terminal;
	@Getter private final LineReader lineReader;

	private InternalJlineManager() throws Exception
	{
		Logger.getLogger( "org.jline" ).setLevel( Level.ALL );
		terminal = TerminalBuilder.builder().dumb( true ).build();
		lineReader = LineReaderBuilder.builder().terminal( terminal ).appName( "Kvantum" ).build();
	}

	public static InternalJlineManager getInstance()
	{
		if ( instance == null )
		{
			try
			{
				instance = new InternalJlineManager();
			} catch ( Exception e )
			{
				e.printStackTrace();
			}
		}
		return instance;
	}

}
