//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.plugin;

import com.plotsquared.iserver.util.Assert;

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
public class PluginManager {

    private final Map<String, Plugin> plugins;

    /**
     * Constructor
     */
    public PluginManager() {
        plugins = new HashMap<>();
    }

    /**
     * Add a plugin to the plugin list, you have to do this before enabling the
     * plugin.
     *
     * @param plugin Plugin to add
     */
    public void addPlugin(final Plugin plugin) {
        Assert.notNull(plugin);
        plugins.put(plugin.toString(), plugin);
    }

    /**
     * Remove a plugin from the list
     *
     * @param plugin Plugin to remove
     */
    public void removePlugin(final Plugin plugin) {
        Assert.notNull(plugin);
        if (plugins.containsKey(plugin.toString()))
            plugins.remove(plugin.toString());
    }

    public Plugin getPlugin(final String providerName) {
        return this.plugins.get(providerName);
    }

    /**
     * Enable a plugin
     *
     * @param plugin Plugin to enable
     * @throws java.lang.RuntimeException if the plugin is not added to the plugin list {@see
     *                                    #addPlugin(com.marine.Plugin)}
     */
    protected void enablePlugin(final Plugin plugin) {
        Assert.notNull(plugin);
        if (!plugins.containsKey(plugin.toString()))
            throw new RuntimeException("Plugin: " + plugin.getName()
                    + " is not added to the plugin list, can't enable");
        plugin.enable();
    }

    /**
     * Disable a plugin
     *
     * @param plugin Plugin to disable
     * @throws java.lang.RuntimeException if the plugin is not added to the plugin list {@see
     *                                    #addPlugin(com.marine.Plugin)}
     */
    protected void disablePlugin(final Plugin plugin) {
        Assert.notNull(plugin);
        if (!plugins.containsKey(plugin.toString()))
            throw new RuntimeException("Plugin: " + plugin.getName()
                    + " is not added to the plugin list, can't disable");
        plugin.disable();
    }

    /**
     * Get a collection containing ALL plugins
     *
     * @return all plugins
     */
    public Collection<Plugin> getPlugins() {
        return plugins.values();
    }

    /**
     * Get the plugins in strng format
     *
     * @return A collection containing the names of the plugins
     */
    public Collection<String> getPluginNames() {
        return plugins.values().stream().map(Plugin::getName).collect(Collectors.toList());
    }

    /**
     * Get a collection containing all ENABLED plugins
     *
     * @return all enabled plugins
     */
    public Collection<Plugin> getEnabledPlugins() {
        return this.plugins.values().stream().filter(Plugin::isEnabled).collect(Collectors.toCollection(ArrayList::new));
    }
}