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
package xyz.kvantum.server.implementation.commands;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.CommandInstance;
import com.intellectualsites.commands.parser.impl.StringParser;
import java.io.File;
import java.util.Collections;
import java.util.UUID;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.config.ConfigurationFile;
import xyz.kvantum.server.api.config.YamlConfiguration;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.views.ViewDetector;

@CommandDeclaration(command = "generate", usage = "/generate <folder> <base uri path>", description = "See: https://github.com/IntellectualSites/Kvantum/wiki/generate") public class Generate
		extends Command
{

	public Generate()
	{
		this.withArgument( "folder", new StringParser(), "Starting folder" );
		this.withArgument( "basePath", new StringParser(), "URI path (use none for default folder path)" );
	}

	@Override public boolean onCommand(final CommandInstance instance)
	{
		final String folderName = instance.getString( "folder" );
		final String basePath;
		if ( instance.getString( "basePath" ).equalsIgnoreCase( "none" ) )
		{
			basePath = "";
		} else
		{
			basePath = instance.getString( "basePath" );
		}
		final Path path = ServerImplementation.getImplementation().getFileSystem().getPath( folderName );
		if ( !path.exists() )
		{
			Logger.error( "No such folder: {}", path.toString() );
			return true;
		}

		final ViewDetector viewDetector = new ViewDetector( basePath, path, Collections.emptyList() );
		final int loaded = viewDetector.loadPaths();
		Logger.info( "Found {} folders inside of {}", loaded, path );
		viewDetector.getPaths().forEach( p -> Logger.info( "- {}", p.toString() ) );
		viewDetector.generateViewEntries();

		final String fileName = UUID.randomUUID().toString();
		final ConfigurationFile configurationFile;
		try
		{
			configurationFile = new YamlConfiguration( fileName,
					new File( new File( ServerImplementation.getImplementation().getCoreFolder(), "config" ),
							fileName + ".yml" ) );
			configurationFile.loadFile();
		} catch ( Exception e )
		{
			Logger.error( "Failed to generate view declaration for {}", path );
			e.printStackTrace();
			return true;
		}
		configurationFile.set( "views", viewDetector.getViewEntries() );
		configurationFile.saveFile();
		Logger.info( "Generated views can be found in 'config/{}.yml'", fileName );
		Logger.info( "Add '{}: {}.yml' to views.yml to load the file", fileName, fileName );
		return true;
	}

}
