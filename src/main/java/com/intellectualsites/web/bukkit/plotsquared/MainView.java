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

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.views.View;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

public class MainView extends View {

    public MainView(String pattern, Map<String, Object> options) {
        super(pattern, "plotsquared", options);
        super.defaultFile = "index";
        super.fileName = "{2}.html";
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        File file = getFile(matcher);
        request.addMeta("ps_file", file);
        return file.exists();
    }
}
