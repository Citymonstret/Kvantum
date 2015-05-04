package com.intellectualsites.web.bukkit.events;

import com.intellectualsites.web.core.Server;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created 2015-05-04 for IntellectualServer
 *
 * @author Citymonstret
 */
public class StartupEvent extends Event {


    public static HandlerList handlers;
    static {
        handlers = new HandlerList();
    }

    private final Server server;
    public StartupEvent(final Server server) {
        this.server = server;
    }

    public Server getServer() {
        return this.server;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
