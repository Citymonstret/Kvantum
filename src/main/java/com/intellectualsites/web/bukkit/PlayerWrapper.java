package com.intellectualsites.web.bukkit;

import org.bukkit.entity.Player;

/**
 * Created 2015-05-10 for IntellectualServer
 *
 * @author Citymonstret
 */
public class PlayerWrapper {

    private final Player player;

    public PlayerWrapper(final Player player) {
        this.player = player;
    }

    @Override
    public String toString() {
        return this.player.getName();
    }

    public Player getPlayer() {
        return this.player;
    }

    public Object get(String p) {
        switch (p.toLowerCase()) {
            case "health":
                return player.getHealth();
            case "address":
                return player.getAddress();
        }
        return "[BukkitPlayer:UnknownProperty]";
    }
}
