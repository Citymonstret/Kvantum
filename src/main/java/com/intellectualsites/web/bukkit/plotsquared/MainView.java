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
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.cache.CacheApplicable;
import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.syntax.Variable;
import com.intellectualsites.web.object.syntax.VariableProvider;
import com.intellectualsites.web.util.FileUtils;
import com.intellectualsites.web.views.View;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class MainView extends View implements CacheApplicable {

    private final File psFolder;

    public MainView(final File psFolder) {
        super("(\\/)?(index)?(.html)?", "plotsquared");
        this.psFolder = psFolder;
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        return true;
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);
        response.setContent(FileUtils.getDocument(new File(psFolder, "index.html"), getBuffer()));
        return response;
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }

    public ProviderFactory getFactory(final Request r) {
        return new ProviderFactory() {

            private final PSProvider provider = new PSProvider();

            @Override
            public VariableProvider get(Request r) {
                return provider;
            }

            @Override
            public String providerName() {
                return "ps";
            }
        };
    }

    private class PSProvider implements VariableProvider {

        private final List<String> validOptions = Arrays.asList("");

        @Override
        public boolean contains(String variable) {
            return validOptions.contains(variable);
        }

        @Override
        public Object get(String variable) {
            return null;
        }
    }
}
