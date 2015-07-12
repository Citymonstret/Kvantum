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

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.cache.CacheApplicable;
import org.lesscss.LessCompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created 2015-04-22 for IntellectualServer
 *
 * @author Citymonstret
 */
public class LessView extends View implements CacheApplicable {

    private static LessCompiler compiler;

    public LessView(String filter, Map<String, Object> options) {
        super(filter, "less", options);
        super.relatedFolderPath = "./assets/less";
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        String file = matcher.group(2);
        if (!file.endsWith(".less"))
            file = file + ".less";
        request.addMeta("less_file", file);

        boolean exists = (new File(getFolder(), file)).exists();
        if (!exists && Server.getInstance().verbose) {
            Server.getInstance().log("Couldn't find less file '%s'", file);
        }

        return matcher.matches() && exists;
    }


    @Override
    public Response generate(final Request r) {
        File file = new File(getFolder(), r.getMeta("less_file").toString());
        StringBuilder document = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file), getBuffer());
            String line;
            while ((line = reader.readLine()) != null) {
                document.append(line).append("\n");
            }
            reader.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_CSS);

        if (compiler == null) {
            compiler = new LessCompiler();
        }
        try {
            response.setContent(compiler.compile(document.toString()));
        } catch(final Exception e) {
            response.setContent("");
            e.printStackTrace();
        }

        return response;
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }
}
