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

package com.intellectualsites.web.bukkit.plotsquared;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.syntax.VariableProvider;
import com.intellectualsites.web.util.FileUtils;
import com.intellectualsites.web.views.View;
import com.plotsquared.bukkit.util.UUIDHandler;
import com.plotsquared.bukkit.util.bukkit.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;

public class UserInfo extends View implements CacheApplicable {

    final File template;

    public UserInfo(final File template) {
        super("(\\/player\\/)([A-Za-z0-9_-]*)", "userinfo");
        this.template = template;
    }

    @Override
    public boolean passes(Matcher matcher, Request r) {
        String username = matcher.group(2);
        UUID uuid =  UUIDHandler.getUUID(username);
        r.addMeta("uuid", uuid);
        return uuid != null;
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);
        response.setContent(FileUtils.getDocument(template, getBuffer()));
        return response;
    }

    /**
     * Get the view specific factory (if it exists)
     *
     * @param r Request IN
     * @return Null by default, or the ProviderFactory (if set by the view)
     */
    public ProviderFactory getFactory(final Request r) {
        return new ProviderFactory() {
            @Override
            public VariableProvider get(Request r) {
                return new UserProvider((UUID) r.getMeta("uuid"));
            }

            @Override
            public String providerName() {
                return "user";
            }
        };
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }

    private class UserProvider implements VariableProvider {

        final UUID uuid;
        final List<String> args = Arrays.asList("uuid", "username", "owner", "trusted", "member", "denied", "online", "offline", "lastjoin", "inplot", "plotsowned");

        UserProvider(UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public boolean contains(String variable) {
            return args.contains(variable.toLowerCase());
        }

        @Override
        public Object get(String variable) {
            Player p = Bukkit.getPlayer(uuid);

            switch(variable.toLowerCase()) {
                case "uuid":
                    return this.uuid.toString();
                case "username":
                    return UUIDHandler.getName(this.uuid);
                case "owner":
                    return getPlotList(PS.get().getPlots(uuid));
                case "trusted":
                    return getPlotList(getPlots(new PlotFilter() {
                        @Override
                        public boolean check(Plot plot) {
                            return plot.getTrusted().contains(uuid);
                        }
                    }));
                case "member":
                    return getPlotList(getPlots(new PlotFilter(){@Override public boolean check(Plot plot){return plot.getMembers().contains(uuid);}}));
                case "denied":
                    return getPlotList(getPlots(new PlotFilter(){@Override public boolean check(Plot plot){return plot.getDenied().contains(uuid);}}));
                case "online":
                    return p != null && p.isOnline();
                case "offline":
                    return p == null || !p.isOnline();
                case "lastjoin":
                    return DateFormat.getDateInstance().format(new Date(Bukkit.getOfflinePlayer(uuid).getLastPlayed()));
                case "inplot":
                    if (p == null || !p.isOnline()) {
                        return "none";
                    } else {
                        if (PS.get().isPlotWorld(p.getWorld().getName())) {
                            Plot plot = MainUtil.getPlot(BukkitUtil.getLocation(p));
                            if (plot != null) {
                                return "<a href=\"/plot/" + plot.world + "/" + plot.getId().toString() + "\" tooltip=\"View Plot Page\">" + plot.id + "</a>";
                            }
                        }
                    }
                    return "none";
                case "plotsowned":
                    return PS.get().getPlots(uuid).size();
                default:
                    return null;
            }
        }

    }

    private abstract class PlotFilter {
        public abstract boolean check(final Plot plot);
    }

    public Set<Plot> getPlots(final PlotFilter filter) {
        Set<Plot> plots = new HashSet<>();
        for (final Plot plot : PS.get().getPlots()) {
            if (filter.check(plot)) {
                plots.add(plot);
            }
        }
        return plots;
    }

    public String[] getPlotList(Collection<Plot> plots) {
        String[] stringArray = new String[plots.size()];
        int index = 0;
        for (Plot plot : plots) {
            stringArray[index++] = "<span class=\"label label-default\"><a href=\"/plot/" + plot.world + "/" + plot.getId() + "\">" + plot.world + ";" + plot.id + "</a></span>";
        }
        return stringArray;
    }

}
