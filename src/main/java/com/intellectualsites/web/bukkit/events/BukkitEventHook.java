package com.intellectualsites.web.bukkit.events;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.events.Event;
import org.bukkit.event.HandlerList;

/**
 * Created 2015-07-04 for IntellectualServer
 *
 * @author Citymonstret
 */
public class BukkitEventHook extends org.bukkit.event.Event {

    private final Class<? extends Event> event;

    public static HandlerList handlers;
    static {
        handlers = new HandlerList();
    }

    public BukkitEventHook(final Class<? extends Event> event) {
        this.event = event;
    }

    public Class<? extends Event> getEvent() {
        return this.event;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
