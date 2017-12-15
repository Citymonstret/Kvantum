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
package xyz.kvantum.server.api.plugin;

import xyz.kvantum.server.api.exceptions.PluginException;
import xyz.kvantum.server.api.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plugin Manager
 * <p>
 * This should be used rather than the @Protected PluginLoader
 *
 * @author Citymonstret
 */
@SuppressWarnings("unused")
public final class PluginManager
{

    private final Map<String, Plugin> plugins;

    /**
     * Constructor
     */
    public PluginManager()
    {
        plugins = new HashMap<>();
    }

    /**
     * Add a plugin to the plugin list, you have to do this before enabling the
     * plugin.
     *
     * @param plugin Plugin to add
     */
    void addPlugin(final Plugin plugin)
    {
        Assert.notNull( plugin );
        plugins.put( plugin.toString(), plugin );
    }

    /**
     * Remove a plugin from the list
     *
     * @param plugin Plugin to remove
     */
    public void removePlugin(final Plugin plugin)
    {
        Assert.notNull( plugin );
        if ( plugins.containsKey( plugin.toString() ) )
            plugins.remove( plugin.toString() );
    }

    public Plugin getPlugin(final String providerName)
    {
        return this.plugins.get( providerName );
    }

    /**
     * Enable a plugin
     *
     * @param plugin Plugin to enable
     * @throws PluginException if the plugin is not added to the plugin list {@see
     *                                    #addPlugin(com.marine.Plugin)}
     */
    void enablePlugin(final Plugin plugin)
    {
        Assert.notNull( plugin );
        if ( !plugins.containsKey( plugin.toString() ) )
        {
            throw new PluginException( "Plugin: " + plugin.getName()
                    + " is not added to the plugin list, can't enable" );
        }
        plugin.enable();
    }

    /**
     * Disable a plugin
     *
     * @param plugin Plugin to disable
     * @throws PluginException if the plugin is not added to the plugin list {@see
     *                                    #addPlugin(com.marine.Plugin)}
     */
    void disablePlugin(final Plugin plugin)
    {
        Assert.notNull( plugin );
        if ( !plugins.containsKey( plugin.toString() ) )
        {
            throw new PluginException( "Plugin: " + plugin.getName()
                    + " is not added to the plugin list, can't disable" );
        }
        plugin.disable();
    }

    /**
     * Get a collection containing ALL plugins
     *
     * @return all plugins
     */
    Collection<Plugin> getPlugins()
    {
        return plugins.values();
    }

    /**
     * Get the plugins in strng format
     *
     * @return A collection containing the names of the plugins
     */
    public Collection<String> getPluginNames()
    {
        return plugins.values().stream().map( Plugin::getName ).collect( Collectors.toList() );
    }

    /**
     * Get a collection containing all ENABLED plugins
     *
     * @return all enabled plugins
     */
    public Collection<Plugin> getEnabledPlugins()
    {
        return this.plugins.values().stream().filter( Plugin::isEnabled ).collect( Collectors.toCollection( ArrayList::new ) );
    }
}
