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

package com.intellectualsites.web.object;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-21 for IntellectualServer
 *
 * @author Citymonstret
 */
public class PostRequest {

    public final String request;
    private final Map<String, String> vars;

    public PostRequest(final String request) {
        this.request = request;
        this.vars = new HashMap<>();
        for (String s : request.split("&")) {
            String[] p = s.split("=");
            vars.put(p[0], p[1].replace("+", " "));
        }
    }

    public String buildLog() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, String> e : vars.entrySet()) {
            b.append(e.getKey()).append("=").append(e.getValue()).append("&");
        }
        return b.toString();
    }

    public String get(String k) {
        return vars.get(k);
    }

    public boolean contains(String k) {
        return vars.containsKey(k);
    }

    public Map<String, String> get() {
        return vars;
    }
}
