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

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.FileUtils;
import com.intellectualsites.web.views.View;
import com.plotsquared.bukkit.util.UUIDHandler;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

public class SearchView extends View {

    final File template, entryTemplate;
    final String entryText;

    public SearchView(final File template, final File entryTemplate) {
        super("(\\/search\\/)(\\?query=([\\S\\s]*))", "plotsearch");
        this.template = template;
        this.entryTemplate = template;
        this.entryText = FileUtils.getDocument(entryTemplate, getBuffer());
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        request.addMeta("psquery",  matcher.group(3).replace("%20", " ").replace("+", " ").replace("%3B", ";"));
        if (Server.getInstance().verbose) {
            Server.getInstance().log("Searching for plots with "  +  matcher.group(3).replace("%20", " ").replace("+", " ").replace("%3B", ";"));
        }
        return true;
    }

    @Override
    public Response generate(final Request in) {
        List<Plot> result = MainUtil.getPlotsBySearch(in.getMeta("psquery").toString());
        StringBuilder resultText = new StringBuilder();
        for (final Plot plot : result) {
            resultText.append(
                    entryText
                    .replace("{{id}}", plot.id.toString())
                    .replace("{{world}}", plot.world)
                    .replace("{{owner}}", UUIDHandler.getName(plot.owner))
            );
        }
        Response response = new Response(this);
        String resultString = resultText.toString();
        if (resultString.length() == 0) {
            resultString = "No results found!"; // Should do something like this if there are no results
        }
        response.setContent(FileUtils.getDocument(template, getBuffer()).replace("{{results}}", resultString));
        return response;
    }
}
