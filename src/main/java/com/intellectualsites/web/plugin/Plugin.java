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

package com.intellectualsites.web.plugin;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.logging.LogProvider;
import com.intellectualsites.web.util.Assert;

import java.io.File;
import java.util.UUID;

/**
 * Plugins will need to implement this class in order to be loaded.
 * <p>
 * Use the methods in here, rather than any external methods that do the same
 * thing, as these are optimized for use in plugins, and will make sure that
 * nothing breaks.
 *
 * @author Citymonstret
 */
public class Plugin implements LogProvider {

    private final UUID uuid;
    private boolean enabled;

    private String name, version, author, provider;

    private File data;

    private PluginClassLoader classLoader;

    private PluginFile desc;

    /**
     * Constructor
     */
    public Plugin() {
        uuid = UUID.randomUUID();
        enabled = false;
    }

    /**
     * Do you want your plugin to tick?
     */
    public void tick() {
    }

    final public void create(final PluginFile desc, final File data,
                             final PluginClassLoader classLoader) {
        if (this.desc != null)
            throw new RuntimeException("Plugin already created: " + desc.name);
        Assert.notNull(desc, data, classLoader);
        this.desc = desc;
        this.classLoader = classLoader;
        name = desc.name;
        this.data = data;
        author = desc.author;
        version = desc.version;
    }

    /**
     * Get the plugin class loader
     *
     * @return Plugin class loader
     */
    final public PluginClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Used to enable the plugin
     *
     * @throws RuntimeException If the plugin is already enabled, or if couldn't be enabled
     */
    final public void enable() {
        Assert.equals(enabled, false);
        try {
            enabled = true;
            onEnable();
        } catch (final Exception e) {
            enabled = false;
            throw new RuntimeException("Could not enable plugin", e);
        }
    }

    /**
     * Used to disable the plugin
     */
    final public void disable() {
        Assert.equals(enabled, true);

        enabled = false;
        onDisable();
    }

    /**
     * Listen to enable
     */
    void onEnable() {
        // Override!
    }

    /**
     * Listen to disable
     */
    void onDisable() {
        // Override!
    }

    /**
     * Get the plugin name
     *
     * @return Plugin name
     */
    final public String getName() {
        return name;
    }

    /**
     * Get the plugin version
     *
     * @return Plugin version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the plugin author
     *
     * @return Plugin author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Check if the plugin is enabled
     *
     * @return boolean (enabled)
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the plugin uuid
     *
     * @return UUID
     */
    final public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the plugin data folder
     *
     * @return Plugin data folder
     */
    public File getDataFolder() {
        return data;
    }

    /**
     * Get the plugin description file
     *
     * @return Plugin Desc. file
     */
    public PluginFile getDesc() {
        return desc;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Plugin
                && ((Plugin) object).getUUID().equals(getUUID());
    }

    @Override
    public int hashCode() {
        return 3 * uuid.hashCode() + 37 * getName().toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getLogIdentifier() {
        return this.toString();
    }

    public void log(String message) {
        Server.getInstance().log(this, message);
    }

}

