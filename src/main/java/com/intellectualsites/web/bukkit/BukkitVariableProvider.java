package com.intellectualsites.web.bukkit;

import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.syntax.VariableProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Created 2015-05-04 for IntellectualServer
 *
 * @author Citymonstret
 */
@SuppressWarnings("ALL")
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
        String v = variable, s = "";
        if (variable.contains("@")) {
            String[] parts = variable.split("@");
            v = parts[0];
            s = parts[1];
        }
        switch (v.toLowerCase()) {
            case "version":
            case "players":
            case "count":
            case "max":
                return true;
            case "player": {
                if (!s.equals("")) {
                    return Bukkit.getPlayer(s) != null;
                }
            }
            default:
                return false;
        }
    }

    @Override
    public Object get(String variable) {
        String v = variable, s = "";
        if (variable.contains("@")) {
            String[] parts = variable.split("@");
            v = parts[0];
            s = parts[1];
        }
        switch (v.toLowerCase()) {
            case "version":
                return Bukkit.getVersion();
            case "players":
                return getPlayers();
            case "count":
                return Bukkit.getOnlinePlayers().size();
            case "max":
                return Bukkit.getMaxPlayers();
            case "player": {
                if (!s.equals("")) {
                    String[] parts = variable.split("@");
                    Player p = Bukkit.getPlayer(parts[1]);
                    if (p != null) {
                        if (parts.length < 3) {
                            return p.getName();
                        }
                        return new PlayerWrapper(p).get(parts[3]);
                    } else {
                        return "[BukkitPlayer:null]";
                    }
                }
            }
            default:
                return null;
        }
    }

    private PlayerWrapper[] getPlayers() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        PlayerWrapper[] playerz = new PlayerWrapper[players.size()];
        int index = 0;
        for (Player player : players) {
            playerz[index++] = new PlayerWrapper(player);
        }
        return playerz;
    }
}
