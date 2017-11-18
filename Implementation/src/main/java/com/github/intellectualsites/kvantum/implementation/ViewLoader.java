package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.config.ConfigurationFile;
import com.github.intellectualsites.kvantum.api.config.YamlConfiguration;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.views.View;

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
