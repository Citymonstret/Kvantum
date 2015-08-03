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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import com.intellectualsites.web.bukkit.SimplePlayerWrapper;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.syntax.VariableProvider;
import com.intellectualsites.web.util.FileUtils;
import com.intellectualsites.web.views.View;

/**
 * Created 7/20/2015 for IntellectualServer
 *
 * @author Citymonstret
 */
public class PlotInfo extends View implements CacheApplicable {

    final File template;

    public PlotInfo(final File template) {
        super("(\\/plot\\/)([A-Za-z0-9_-]*)\\/(([\\-0-9]*);([\\-0-9]*))", "plotinfo");
        this.template = template;
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String worldName = matcher.group(2);
        if (!PS.get().isPlotWorld(worldName)) {
            return false;
        }
        PlotId id = new PlotId(Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)));
        Plot plot;
        if ((plot = MainUtil.getPlot(worldName, id)) == null) {
            return false;
        }
        request.addMeta("plot", plot);
        return true;
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
                return new PlotProvider((Plot) r.getMeta("plot"));
            }

            @Override
            public String providerName() {
                return "plot";
            }
        };
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }

    private class PlotProvider implements VariableProvider {

        final Plot plot;
        final List<String> args = Arrays.asList("id", "alias", "owner", "biome", "rating", "trusted", "members", "denied", "flags", "world", "schematic", "inplot", "top", "bottom", "home");

        PlotProvider(final Plot plot) {
            this.plot = plot;
        }

        @Override
        public boolean contains(String variable) {
            return args.contains(variable.toLowerCase());
        }

        @Override
        public Object get(String variable) {
            switch(variable.toLowerCase()) {
                case "id":
                    return plot.id.toString();
                case "alias":
                    return plot.toString();
                case "owner":
                    return UUIDHandler.getName(plot.owner);
                case "biome": {
                    Location top = MainUtil.getPlotTopLoc(plot.world, plot.id);
                    Location bot = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
                    return BlockManager.manager.getBiome(bot.add((top.getX() - bot.getX()) / 2, 0, (top.getX() - bot.getX()) / 2));
                }
                case "inplot":
                    return getPlayersInPlot(plot);
                case "rating":
                    return (int)(plot.getAverageRating());
                case "trusted":
                    return getPlayers(plot.getTrusted());
                case "members":
                    return getPlayers(plot.getMembers());
                case "denied":
                    return getPlayers(plot.getDenied());
                case "flags":
                    return toStringCollection(FlagManager.getPlotFlags(plot).values());
                case "world":
                    return plot.world;
                case "top":
                    return getSimpleLoc(MainUtil.getPlotTopLoc(plot.world, plot.id));
                case "bottom":
                    return getSimpleLoc(MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1));
                case "home":
                    return getSimpleLoc(MainUtil.getPlotHome(plot));
                case "schematic": {
                    File file = new File(Settings.SCHEMATIC_SAVE_PATH, plot.id.toString() + "," + plot.world + "," + UUIDHandler.getName(plot.owner) + ".schematic");
                    if (!file.exists()) {
                        return "none";
                    } else {
                        return "<a href=\"/schematic/" + plot.id.toString() + "," + plot.world + "," + UUIDHandler.getName(plot.owner) + ".schematic\">download</a>";
                    }
                }
                default:
                    return null;
            }
        }

    }

    public String getSimpleLoc(Location location) {
        return "X:" + location.getX() + ", Y:" + location.getY() + ", Z:" + location.getZ();
    }

    public Collection<SimplePlayerWrapper> getPlayersInPlot(final Plot plot) {
        List<SimplePlayerWrapper> l = new ArrayList<>();
        for (PlotPlayer pp : UUIDHandler.players.values()) {
            if (plot.equals(MainUtil.getPlot(pp.getLocation()))) {
                l.add(new SimplePlayerWrapper(pp.getUUID()));
            }
        }
        return l;
    }

    public <T> Collection<String> toStringCollection(Collection<T> in) {
        Collection<String> newCollection = new ArrayList<>();
        for (T t : in) {
            newCollection.add(t.toString());
        }
        return newCollection;
    }

    public SimplePlayerWrapper[] getPlayers(final Collection<UUID> players) {
        SimplePlayerWrapper[] wrappers = new SimplePlayerWrapper[players.size()];
        int i = 0;
        for (UUID player : players) {
            wrappers[i++] = new SimplePlayerWrapper(player);
        }
        return wrappers;
    }
}
