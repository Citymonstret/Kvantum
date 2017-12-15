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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.sf.oval.constraint.NotNull;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.exceptions.PluginException;
import xyz.kvantum.server.api.logging.LogProvider;
import xyz.kvantum.server.api.util.Assert;

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
@EqualsAndHashCode(of = "uuid")
@SuppressWarnings({ "WeakerAccess", "unused" })
@NoArgsConstructor
public class Plugin implements LogProvider
{

    private final UUID uuid = UUID.randomUUID();
    protected String name, version, author, provider;
    @Getter
    private PluginClassLoader classLoader;
    private boolean enabled = false;
    private File data;
    private PluginFile desc;

    final public void create(final PluginFile desc, final File data,
                             final PluginClassLoader classLoader)
    {
        if ( this.desc != null )
        {
            throw new PluginException( "Plugin already created: " + desc.name );
        }
        Assert.notNull( desc, data, classLoader );
        this.desc = desc;
        this.classLoader = classLoader;
        name = desc.name;
        this.data = data;
        author = desc.author;
        version = desc.version;
    }

    /**
     * Used to enable the plugin
     *
     * @throws PluginException If the plugin is already enabled, or if couldn't be enabled
     */
    final void enable()
    {
        Assert.equals( enabled, false );
        try
        {
            enabled = true;
            onEnable();
        } catch ( final Exception e )
        {
            enabled = false;
            throw new PluginException( "Could not enable plugin", e );
        }
    }

    /**
     * Used to disable the plugin
     */
    final void disable()
    {
        Assert.equals( enabled, true );

        enabled = false;
        onDisable();
    }

    /**
     * Listen to enable
     */
    @SuppressWarnings("ALL")
    protected void onEnable()
    {
        // Override!
    }

    /**
     * Listen to disable
     */
    @SuppressWarnings("ALL")
    protected void onDisable()
    {
        // Override!
    }

    /**
     * Get the plugin name
     *
     * @return Plugin name
     */
    final public String getName()
    {
        return this.name;
    }

    /**
     * Get the plugin version
     *
     * @return Plugin version
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * Get the plugin author
     *
     * @return Plugin author
     */
    public String getAuthor()
    {
        return this.author;
    }

    /**
     * Check if the plugin is enabled
     *
     * @return boolean (enabled)
     */
    public boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     * Get the plugin uuid
     *
     * @return UUID
     */
    private UUID getUUID()
    {
        return this.uuid;
    }

    /**
     * Get the plugin data folder
     *
     * @return Plugin data folder
     */
    public File getDataFolder()
    {
        return this.data;
    }

    /**
     * Get the plugin description file
     *
     * @return Plugin Desc. file
     */
    public PluginFile getDesc()
    {
        return this.desc;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    @Override
    public String getLogIdentifier()
    {
        return this.toString();
    }

    /**
     * Log a message
     *
     * @param message Message to be logged
     * @see Kvantum#log(String, Object...)
     */
    public void log(@NotNull final String message)
    {
        ServerImplementation.getImplementation().log( this, message );
    }

}

