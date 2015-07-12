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
