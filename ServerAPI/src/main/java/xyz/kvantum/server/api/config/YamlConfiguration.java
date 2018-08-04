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
package xyz.kvantum.server.api.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import xyz.kvantum.server.api.exceptions.KvantumException;
import xyz.kvantum.server.api.util.Assert;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
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
            throw new KvantumException( "Couldn't create parents for " + file.getAbsolutePath() );
        }
        if ( !file.exists() && !file.createNewFile() )
        {
            throw new KvantumException( "Couldn't create " + file.getAbsolutePath() );
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
    public final void reload()
    {
        this.map = new HashMap<>();
        this.loadFile();
    }

    @Override
    public final void saveFile()
    {
        try ( BufferedWriter writer = new BufferedWriter( new FileWriter( file ) ) )
        {
            this.getYaml().dump( map, writer );
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ALL")
    @Override
    public final void loadFile()
    {
        try ( BufferedInputStream stream = new BufferedInputStream( new FileInputStream( file ) ) )
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
    public final <T> void set(final String key, final T value)
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

    @SuppressWarnings("ALL")
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
    public final <T> T get(final String key, final T def)
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
    public final <T> T get(final String key)
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
    public final Map<String, Object> getAll()
    {
        return new HashMap<>( this.map );
    }

    @Override
    @SuppressWarnings("ALL")
    public final boolean contains(final String key)
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
    public final <T> void setIfNotExists(final String key, final T value)
    {
        Assert.notNull( key, value );

        if ( !this.contains( key ) )
        {
            this.set( key, value );
        }
    }
}
