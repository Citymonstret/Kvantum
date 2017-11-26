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
package xyz.kvantum.server.implementation;

import xyz.kvantum.server.api.config.ConfigurationFile;
import xyz.kvantum.server.api.config.YamlConfiguration;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.views.View;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ALL")
public class ViewLoader
{

    private Map<String, Map<String, Object>> views = new HashMap<>();

    ViewLoader(final ConfigurationFile viewConfiguration)
    {
        this.addViews( viewConfiguration );
        this.views.entrySet().forEach( this::loadView );
    }

    ViewLoader(final Map<String, Map<String, Object>> views)
    {
        this.views = views;
        this.views.entrySet().forEach( this::loadView );
    }

    private void visitMembers(final Map<String, Object> views)
    {
        for ( final Map.Entry<String, Object> viewEntry : views.entrySet() )
        {
            if ( viewEntry.getValue() instanceof String )
            {
                final String object = viewEntry.getValue().toString();
                final ConfigurationFile includeFile;
                if ( object.endsWith( ".yml" ) )
                {
                    try
                    {
                        includeFile = new YamlConfiguration( object,
                                new File( new File( Server.getInstance().getCoreFolder(), "config" ), object ) );
                        includeFile.loadFile();
                        if ( !includeFile.contains( "views" ) )
                        {
                            continue;
                        }
                    } catch ( Exception e )
                    {
                        e.printStackTrace();
                        continue;
                    }
                } else
                {
                    Logger.warn( "Trying to include view declaration " +
                            "that is not of YAML type: %s", object );
                    continue;
                }
                this.addViews( includeFile );
            } else
            {
                final Object rawObject = viewEntry.getValue();
                if ( rawObject instanceof Map )
                {
                    this.views.put( viewEntry.getKey(), (Map<String, Object>) viewEntry.getValue() );
                }
            }
        }
    }

    private void loadView(final Map.Entry<String, Map<String, Object>> viewEntry)
    {
        final Map<String, Object> viewBody = (Map<String, Object>) viewEntry.getValue();
        if ( !validateView( viewBody ) )
        {
            Logger.warn( "Invalid view declaration: %s", viewEntry.getKey() );
            return;
        }
        final String type = viewBody.get( "type" ).toString().toLowerCase( Locale.US );
        final String filter = viewBody.get( "filter" ).toString();
        final Map<String, Object> options = (Map<String, Object>) viewBody.getOrDefault( "options", new HashMap<>() );
        options.put( "internalName", viewEntry.getKey() );
        if ( Server.getInstance().getViewBindings().containsKey( type ) )
        {
            final Class<? extends View> vc = Server.getInstance().getViewBindings()
                    .get( type.toLowerCase( Locale.US ) );
            try
            {
                final View vv = vc.getDeclaredConstructor( String.class, Map.class )
                        .newInstance( filter, options );
                ServerImplementation.getImplementation().getRouter().add( vv );
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        } else
        {
            Logger.warn( "View declaration '%s' trying to declare unknown type: %s",
                    viewEntry.getKey(), type );
        }
    }

    private boolean validateView(final Map<String, Object> viewBody)
    {
        return viewBody.containsKey( "type" ) && viewBody.containsKey( "filter" );
    }

    private void addViews(final ConfigurationFile file)
    {
        final Map<String, Object> rawEntries = file.get( "views" );
        this.visitMembers( rawEntries );
    }

}
