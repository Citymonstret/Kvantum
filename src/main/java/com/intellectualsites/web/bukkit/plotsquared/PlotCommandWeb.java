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
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualsites.web.core.Server;

public class PlotCommandWeb extends SubCommand {

    public PlotCommandWeb() {
        super("web", "plots.web", "Get the link to the plot you're standing on", "", "w", CommandCategory.INFO, true);
    }

    @Override
    public boolean execute(PlotPlayer plotPlayer, String... strings) {
        if (plotPlayer == null) {
            sendMessage(null, C.IS_CONSOLE);
        } else {
            if (PS.get().isPlotWorld(plotPlayer.getLocation().getWorld())) {
                Plot p = MainUtil.getPlot(plotPlayer.getLocation());
                if (p != null) {
                    String s = StringMan.replaceFromMap("$1The URL for this plot is: $2http://" + Server.getInstance().hostName + "/plot/" + p.world + "/" + p.id.toString(), C.replacements);
                    MainUtil.sendMessage(plotPlayer, s);
                }
                return true;
            }
            sendMessage(plotPlayer, C.NOT_IN_PLOT);
        }
        return true;
    }
}
