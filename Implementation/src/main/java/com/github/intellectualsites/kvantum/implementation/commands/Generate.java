/*
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
package com.github.intellectualsites.kvantum.implementation.commands;

import com.github.intellectualsites.kvantum.api.config.ConfigurationFile;
import com.github.intellectualsites.kvantum.api.config.YamlConfiguration;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.util.MapBuilder;
import com.github.intellectualsites.kvantum.files.Path;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.CommandInstance;
import com.intellectualsites.commands.parser.impl.StringParser;

import java.io.File;
import java.util.*;

@CommandDeclaration(
        command = "generate",
        usage = "/generate <folder> <base uri path>",
        description = "See: https://github.com/IntellectualSites/Kvantum/wiki/generate"
)
public class Generate extends Command
{

    public Generate()
    {
        this.withArgument( "folder", new StringParser(), "Starting folder" );
        this.withArgument( "basePath", new StringParser(), "URI path (use none for default folder path)" );
    }

    @Override
    public boolean onCommand(final CommandInstance instance)
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
            Logger.error( "No such folder: %s", path.toString() );
            return true;
        }
        final Set<Path> paths = new HashSet<>();
        paths.add( path );
        addSubPaths( paths, path );
        Logger.info( "Found %s folder inside of %s", paths.size(), path );
        paths.forEach( p -> Logger.info( "- %s", p.toString() ) );
        final Map<String, Map<String, Object>> viewEntries = new HashMap<>();
        paths.forEach( p -> loadSubPath( viewEntries, basePath, path.toString(), p ) );
        final String fileName = UUID.randomUUID().toString();
        final ConfigurationFile configurationFile;
        try
        {
            configurationFile = new YamlConfiguration( fileName,
                    new File( new File( ServerImplementation.getImplementation().getCoreFolder(),
                            "config" ), fileName + ".yml" ) );
            configurationFile.loadFile();
        } catch ( Exception e )
        {
            Logger.error( "Failed to generate view declaration for %s", path );
            e.printStackTrace();
            return true;
        }
        configurationFile.set( "views", viewEntries );
        configurationFile.saveFile();
        Logger.info( "Generated views can be found in 'config/%s.yml'", fileName );
        Logger.info( "Add '%s: %s.yml' to views.yml to load the file", fileName, fileName );
        return true;
    }

    private void loadSubPath(final Map<String, Map<String, Object>> viewEntries, final String basePath,
                             final String toRemeove, final Path path)
    {
        String extension = null;
        boolean moreThanOneType = false;
        boolean hasIndex = false;
        String indexExtension = "";

        for ( final Path subPath : path.getSubPaths( false ) )
        {
            if ( extension == null )
            {
                extension = subPath.getExtension();
            } else if ( !extension.equalsIgnoreCase( subPath.getExtension() ) )
            {
                moreThanOneType = true;
            }
            if ( !hasIndex )
            {
                hasIndex = subPath.getEntityName().equals( "index" );
                indexExtension = subPath.getExtension();
            }
        }

        if ( extension == null )
        {
            return;
        }

        final String type;
        if ( moreThanOneType )
        {
            type = "std";
        } else
        {
            switch ( extension )
            {
                case "html":
                    type = "html";
                    break;
                case "js":
                    type = "javascript";
                    break;
                case "css":
                    type = "css";
                    break;
                case "less":
                    type = "less";
                    break;
                case "png":
                case "jpg":
                case "jpeg":
                case "ico":
                    type = "img";
                    break;
                case "zip":
                case "txt":
                case "pdf":
                    type = "download";
                    break;
                default:
                    type = "std";
                    break;
            }
        }

        final String folder = "./" + path.toString();
        final String viewPattern;
        if ( moreThanOneType )
        {
            if ( hasIndex )
            {
                viewPattern = ( path.toString().replace( toRemeove, basePath ) ) +
                        "[file=index].[extension=" + indexExtension + "]";
            } else
            {
                viewPattern = ( path.toString().replace( toRemeove, basePath ) ) + "<file>.<extension>";
            }
        } else
        {
            if ( hasIndex )
            {
                viewPattern = ( path.toString().replace( toRemeove, basePath ) ) +
                        "[file=index].[extension=" + indexExtension + "]";
            } else
            {
                viewPattern = ( path.toString().replace( toRemeove, basePath ) ) + "<file>." + extension;
            }
        }

        final Map<String, Object> info = MapBuilder.<String, Object>newHashMap()
                .put( "filter", viewPattern )
                .put( "options", MapBuilder.newHashMap()
                        .put( "folder", folder )
                        .get() )
                .put( "type", type ).get();

        viewEntries.put( UUID.randomUUID().toString(), info );
    }

    private void addSubPaths(final Set<Path> set, final Path path)
    {
        for ( final Path subPath : path.getSubPaths() )
        {
            if ( !subPath.isFolder() )
            {
                continue;
            }
            set.add( subPath );
            addSubPaths( set, subPath );
        }
    }
}
