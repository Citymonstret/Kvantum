package com.intellectualsites.web.bukkit.events;

import com.intellectualsites.web.core.Server;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created 2015-05-04 for IntellectualServer
 *
 * @author Citymonstret
 */
public class StartupEvent extends BukkitEventHook {

    public StartupEvent() {
        super(com.intellectualsites.web.events.defaultEvents.StartupEvent.class);
    }

}
