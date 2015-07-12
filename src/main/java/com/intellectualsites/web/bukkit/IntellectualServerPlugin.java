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

package com.intellectualsites.web.bukkit;

import com.intellectualsites.web.bukkit.events.BukkitEventHook;
import com.intellectualsites.web.core.Bootstrap;
import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.events.Event;
import com.intellectualsites.web.events.EventCaller;
import com.intellectualsites.web.object.LogWrapper;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

/**
 * Created 2015-05-04 for IntellectualServer
 *
 * @author Citymonstret
 */
public class IntellectualServerPlugin extends JavaPlugin implements Listener {

    private static Server server;

    public static Server getIntellectualServer() {
        return IntellectualServerPlugin.server;
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                server = Bootstrap.startServer(false, getDataFolder(), new LogWrapper() {
                    @Override
                    public void log(String s) {
                        getLogger().log(Level.INFO, s);
                    }
                });
                server.setEventCaller(new BukkitEventCaller());
                server.addProviderFactory(new BukkitVariableProvider());
            }
        });
        getServer().getPluginManager().registerEvents(this, this);

    }

    /**
     * A customized event caller, that will
     * hook into the bukkit even caller and use
     * that instead - cheaty, but it works!
     */
    protected class BukkitEventCaller extends EventCaller {

        private final Collection<BukkitEventHook> hooks;

        BukkitEventCaller() {
            hooks = new ArrayList<>();
            hooks.add(new com.intellectualsites.web.bukkit.events.StartupEvent());
        }

        @Override
        public void callEvent(final Event event) {
            for (final BukkitEventHook hook : hooks) {
                if (hook.getEvent().isInstance(event)) {
                    try {
                        BukkitEventHook h = hook.getClass().newInstance();
                        Bukkit.getPluginManager().callEvent(h);
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
