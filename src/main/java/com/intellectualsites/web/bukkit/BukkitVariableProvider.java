package com.intellectualsites.web.bukkit;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.VariableProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Created 2015-05-04 for IntellectualServer
 *
 * @author Citymonstret
 */
public class BukkitVariableProvider implements ProviderFactory<BukkitVariableProvider>, VariableProvider {

    @Override
    public BukkitVariableProvider get(Request r) {
        return this;
    }

    @Override
    public String providerName() {
        return "bukkit";
    }

    @Override
    public boolean contains(String variable) {
        switch (variable.toLowerCase()) {
            case "version":
            case "players":
            case "count":
            case "max":
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object get(String variable) {
        switch (variable.toLowerCase()) {
            case "version":
                return Bukkit.getVersion();
            case "players":
                return getPlayers();
            case "count":
                return Bukkit.getOnlinePlayers().size();
            case "max":
                return Bukkit.getMaxPlayers();
            default:
                return null;
        }
    }

    public PlayerWrapper[] getPlayers() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        PlayerWrapper[] playerz = new PlayerWrapper[players.size()];
        int index = 0;
        for (Player player : players) {
            playerz[index++] = new PlayerWrapper(player);
        }
        return playerz;
    }
}
