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

package com.intellectualsites.web.views;

import com.intellectualsites.web.config.YamlConfiguration;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.syntax.IgnoreSyntax;
import lombok.NonNull;

import java.io.File;
import java.util.Map;

public class RedirectView extends View implements IgnoreSyntax {

    private final YamlConfiguration configuration;

    public RedirectView(@NonNull final String pattern, @NonNull final Map<String, Object> options) {
        super(pattern, "redirect", options);
        super.relatedFolderPath = "./redirect";
        final File file = new File(getFolder(), "redirect.yml");
        try {
            configuration = new YamlConfiguration("redirect", file);
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
        configuration.loadFile();
    }

    @Override
    public boolean passes(Request request) {
        Map<String, String> variables = request.getVariables();
        if (this.configuration.contains(variables.get("entry"))) {
            request.addMeta("redirect_url", configuration.get(variables.get("entry")));
            return true;
        }
        return false;
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);
        response.getHeader().redirect(r.getMeta("redirect_url").toString());
        return response;
    }
}
