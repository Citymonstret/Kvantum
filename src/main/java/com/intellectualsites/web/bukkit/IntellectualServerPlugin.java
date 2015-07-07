package com.intellectualsites.web.bukkit;

import com.intellectualsites.web.bukkit.events.BukkitEventHook;
import com.intellectualsites.web.core.Bootstrap;
import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.events.Event;
import com.intellectualsites.web.events.defaultEvents.StartupEvent;
import com.intellectualsites.web.object.EventCaller;
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
        server = Bootstrap.startServer(true, getDataFolder());
        server.setEventCaller(new BukkitEventCaller());
        server.addProviderFactory(new BukkitVariableProvider());

        getServer().getPluginManager().registerEvents(this, this);

    }

    /**
     * A customized event caller, that will
     * hook into the bukkit even caller and use
     * that instead - cheaty, but it works!
     */
    protected class BukkitEventCaller extends EventCaller {

        private Collection<BukkitEventHook> hooks;

        protected BukkitEventCaller() {
            hooks = new ArrayList<>();
            hooks.add(new com.intellectualsites.web.bukkit.events.StartupEvent());
        }

        @Override
        public void callEvent(final Event event) {
            for (final BukkitEventHook hook : hooks) {
                if (hook.getEvent().isInstance(event)) {
                    try {
                        BukkitEventHook h = (BukkitEventHook) hook.getClass().newInstance();
                        Bukkit.getPluginManager().callEvent(h);
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
