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

import com.intellectualsites.web.object.Header;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.cache.CacheApplicable;
import org.lesscss.LessCompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

/**
 * Created 2015-04-22 for IntellectualServer
 *
 * @author Citymonstret
 */
public class LessView extends View implements CacheApplicable {

    public static LessCompiler compiler;

    public LessView(String filter, Map<String, Object> options) {
        super(filter, "less", options);
        super.relatedFolderPath = "./assets/less";
        super.fileName = "{2}.less";
    }

    @Override
    public boolean passes(Request request) {
        File file = getFile(request);
        request.addMeta("less_file", file);
        return file.exists();
    }


    @Override
    public Response generate(final Request r) {
        File file = (File) r.getMeta("less_file");
        Response response = new Response(this);
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_CSS);
        response.setContent(getLess(file, getBuffer()));
        return response;
    }

    public static String getLess(File file, int buffer) {
        StringBuilder document = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file), buffer);
            String line;
            while ((line = reader.readLine()) != null) {
                document.append(line).append("\n");
            }
            reader.close();
        } catch(final Exception e) {
            e.printStackTrace();
        }

        if (compiler == null) {
            compiler = new LessCompiler();
        }
        try {
            return compiler.compile(document.toString());
        } catch(final Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public boolean isApplicable(Request r) {
        return true;
    }
}
