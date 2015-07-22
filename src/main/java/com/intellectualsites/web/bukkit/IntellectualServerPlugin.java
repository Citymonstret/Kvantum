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

import com.intellectualsites.web.bukkit.hooks.Hook;
import com.intellectualsites.web.bukkit.hooks.PlotSquaredHook;
import com.intellectualsites.web.core.Bootstrap;
import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.events.Event;
import com.intellectualsites.web.events.EventCaller;
import com.intellectualsites.web.object.LogWrapper;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created 2015-05-04 for IntellectualServer
 *
 * @author Citymonstret
 */
public class IntellectualServerPlugin extends JavaPlugin implements Listener {

    private static Server server;

    private List<Hook> hooks;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("enable", false);
        getConfig().addDefault("hook.plotsquared", false);
        saveConfig();

        boolean enable = getConfig().getBoolean("enable");
        boolean hook_plotsquared = getConfig().getBoolean("hook.plotsquared");

        if (enable) {
            hooks = new ArrayList<>();
            if (hook_plotsquared) {
                hooks.add(new PlotSquaredHook());
            }
            Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    server = Bootstrap.createServer(false, getDataFolder(), new LogWrapper() {
                        @Override
                        public void log(String prefix, String prefix1, String timeStamp, String message) {
                            log(message);
                        }

                        @Override
                        public void log(String s) {
                            getLogger().log(Level.INFO, s);
                        }
                    });
                    server.setEventCaller(new BukkitEventCaller());
                    server.addProviderFactory(new BukkitVariableProvider());
                    for (Hook hook : hooks) {
                        hook.load(server);
                    }
                    server.silent = true;
                    server.start();
                }
            });
            getServer().getPluginManager().registerEvents(this, this);
        } else {
            getLogger().log(Level.WARNING, "The webserver is not enabled!");
        }
    }

    @Override
    public void onDisable() {
        Server.getInstance().stopServer();
    }

    /**
     * A customized event caller, that will
     * hook into the bukkit even caller and use
     * that instead - cheaty, but it works!
     */
    protected class BukkitEventCaller extends EventCaller {

        BukkitEventCaller() {}

        @Override
        public void callEvent(final Event event) {}

    }

}
