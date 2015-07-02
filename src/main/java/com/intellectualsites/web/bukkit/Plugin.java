package com.intellectualsites.web.bukkit;

import com.intellectualsites.web.core.Bootstrap;
import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.events.Event;
import com.intellectualsites.web.events.defaultEvents.StartupEvent;
import com.intellectualsites.web.object.EventCaller;
import com.intellectualsites.web.object.IntellectualServerStartException;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Created 2015-05-04 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Plugin extends JavaPlugin implements Listener {

    private Server server;

    @Override
    public void onEnable() {
        server = Bootstrap.startServer(true, getDataFolder());
        server.setEventCaller(new BukkitEventCaller());
        server.addProviderFactory(new BukkitVariableProvider());

        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().log(Level.INFO, "The web server is starting in 15 seconds...");

        final Server finalized = this.server;
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                try {
                    finalized.start();
                } catch (IntellectualServerStartException e) {
                    e.printStackTrace();
                }
            }
        }, 20l * 15);

    }

    protected class BukkitEventCaller extends EventCaller {

        @Override
        public void callEvent(Event event) {
            if (event instanceof StartupEvent) {
                Bukkit.getPluginManager().callEvent(new com.intellectualsites.web.bukkit.events.StartupEvent(((StartupEvent) event).getServer()));
            }
        }
    }

}
