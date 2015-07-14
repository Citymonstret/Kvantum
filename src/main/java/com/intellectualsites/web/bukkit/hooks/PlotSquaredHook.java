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

package com.intellectualsites.web.bukkit.hooks;

import com.intellectualsites.web.bukkit.plotsquared.GetSchematic;
import com.intellectualsites.web.bukkit.plotsquared.MainView;
import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.logging.LogProvider;

import java.io.File;

public class PlotSquaredHook extends Hook implements LogProvider {

    private File file;

    @Override
    public void load(Server server) {
        file = new File(server.coreFolder, "plotsquared");
        if (!file.mkdirs()) {
            log("Couldn't create the main folder ('%s')", file);
            return;
        }
        server.addViewBinding("plotsquared", MainView.class);
        server.getViewManager().add(new GetSchematic());
    }

    @Override
    public String getLogIdentifier() {
        return "PSHook";
    }

    private void log(final String s, final Object ... o) {
        Server.getInstance().log(this, s, o);
    }
}
