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
package com.github.intellectualsites.kvantum.api.config;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;
import com.github.intellectualsites.kvantum.api.util.VariableProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This allows you to access configuration
 * variables through the variable syntax
 *
 * @author Citymonstret
 */
public class ConfigVariableProvider implements ProviderFactory<ConfigVariableProvider>, VariableProvider
{

    private static ConfigVariableProvider instance;
    private final Map<String, ConfigProvider> configurations;

    private ConfigVariableProvider()
    {
        configurations = new HashMap<>();
    }

    public static ConfigVariableProvider getInstance()
    {
        if ( instance == null )
        {
            instance = new ConfigVariableProvider();
        }
        return instance;
    }

    public void add(final ConfigProvider provider)
    {
        configurations.put( provider.toString(), provider );
    }

    @Override
    public Optional<ConfigVariableProvider> get(AbstractRequest r)
    {
        return Optional.of( this );
    }

    @Override
    public String providerName()
    {
        return "cfg";
    }

    @Override
    public boolean contains(final String variable)
    {
        String[] parts = variable.split( "@" );
        return configurations.containsKey( parts[ 0 ] ) && configurations.get( parts[ 0 ] ).contains( parts[ 1 ] );
    }

    @Override
    public Object get(final String variable)
    {
        String[] parts = variable.split( "@" );
        return configurations.get( parts[ 0 ] ).get( parts[ 1 ] );
    }

    @Override
    public Map<String, Object> getAll()
    {
        final Map<String, Object> all = new HashMap<>();
        for ( final Map.Entry<String, ConfigProvider> entry1 : configurations.entrySet() )
        {
            for ( Map.Entry<String, Object> entry2 : entry1.getValue().getAll().entrySet() )
            {
                all.put( entry1.getKey() + "@" + entry2.getKey(), entry2.getValue() );
            }
        }
        return all;
    }
}
