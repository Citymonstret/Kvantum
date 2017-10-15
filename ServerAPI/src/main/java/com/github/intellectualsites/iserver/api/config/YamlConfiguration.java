/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.config;

import com.github.intellectualsites.iserver.api.util.Assert;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * YAML implementation of
 * the configuration file
 *
 * @author Citymonstret
 */
public class YamlConfiguration extends ConfigProvider implements ConfigurationFile
{

    private final File file;
    private Map<String, Object> map;
    private Yaml yaml;

    public YamlConfiguration(final String name, final File file) throws Exception
    {
        super( name );

        Assert.notNull( file );

        this.file = file;
        if ( !file.getParentFile().exists() && !file.getParentFile().mkdirs() )
        {
            throw new RuntimeException( "Couldn't create parents for " + file.getAbsolutePath() );
        }
        if ( !file.exists() && !file.createNewFile() )
        {
            throw new RuntimeException( "Couldn't create " + file.getAbsolutePath() );
        }
        this.map = new HashMap<>();
    }

    private Yaml getYaml()
    {
        if ( yaml == null )
        {
            final DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
            options.setAllowReadOnlyProperties( true );
            options.setAllowUnicode( true );
            options.setPrettyFlow( true );
            this.yaml = new Yaml( options );
        }
        return yaml;
    }

    @Override
    public void reload()
    {
        this.map = new HashMap<>();
        this.loadFile();
    }

    @Override
    public void saveFile()
    {
        try ( final BufferedWriter writer = new BufferedWriter( new FileWriter( file ) ) )
        {
            this.getYaml().dump( map, writer );
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ALL")
    @Override
    public void loadFile()
    {
        try ( final BufferedInputStream stream = new BufferedInputStream( new FileInputStream( file ) ) )
        {
            final Object o = this.getYaml().load( stream );
            if ( o != null )
            {
                this.map.putAll( (HashMap<String, Object>) o );
            }
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public <T> void set(final String key, final T value)
    {
        Assert.notNull( key, value );

        if ( key.contains( "." ) )
        {
            this.convertToMap( key, value );
        } else
        {
            this.map.put( key, value );
        }
    }

    private void convertToMap(String in, final Object value)
    {
        Assert.notNull( in, value );

        if ( in.contains( "." ) )
        {
            Map<String, Object> lastMap = this.map;
            while ( in.contains( "." ) )
            {
                final String[] parts = in.split( "\\." );
                if ( lastMap.containsKey( parts[ 0 ] ) )
                {
                    final Object o = lastMap.get( parts[ 0 ] );
                    if ( o instanceof Map )
                    {
                        lastMap = (Map) o;
                    }
                } else
                {
                    lastMap.put( parts[ 0 ], new HashMap<>() );
                    lastMap = (Map) lastMap.get( parts[ 0 ] );
                }
                final StringBuilder b = new StringBuilder();
                for ( int i = 1; i < parts.length; i++ )
                {
                    b.append( "." ).append( parts[ i ] );
                }
                in = b.toString().replaceFirst( "\\.", "" );
            }
            if ( !lastMap.containsKey( in ) )
            {
                lastMap.put( in, value );
            }
        }
    }

    @Override
    public <T> T get(final String key, final T def)
    {
        Assert.notNull( key, def );

        if ( !this.contains( key ) )
        {
            this.setIfNotExists( key, def );
            return def;
        }
        return this.get( key );
    }

    @SuppressWarnings("ALL")
    @Override
    public <T> T get(final String key)
    {
        Assert.notNull( key );

        if ( this.map.containsKey( key ) )
        {
            return (T) this.map.get( key );
        } else
        {
            if ( key.contains( "." ) )
            {
                final String[] parts = key.split( "\\." );
                Map<String, Object> lastMap = this.map;
                for ( String p : parts )
                {
                    if ( lastMap.containsKey( p ) )
                    {
                        final Object o = lastMap.get( p );
                        if ( o instanceof Map )
                        {
                            lastMap = (Map) o;
                        } else
                        {
                            return (T) o;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getAll()
    {
        return new HashMap<>( this.map );
    }

    @Override
    public boolean contains(final String key)
    {
        Assert.notNull( key );

        if ( this.map.containsKey( key ) )
        {
            return true;
        } else
        {
            if ( key.contains( "." ) )
            {
                final String[] parts = key.split( "\\." );
                Map<String, Object> lastMap = this.map;
                for ( String p : parts )
                {
                    if ( lastMap.containsKey( p ) )
                    {
                        final Object o = lastMap.get( p );
                        if ( o instanceof Map )
                        {
                            lastMap = (Map) o;
                        } else
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public <T> void setIfNotExists(final String key, final T value)
    {
        Assert.notNull( key, value );

        if ( !this.contains( key ) )
        {
            this.set( key, value );
        }
    }
}
