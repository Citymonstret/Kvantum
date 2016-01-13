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

import com.intellectualsites.web.object.syntax.VariableProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Session implements VariableProvider {

    private static long id = 0L;

    private long sessionId = 0;
    private final Map<String, Object> sessionStorage;

    public Session() {
        sessionStorage = new HashMap<>();
        sessionId = id++;
    }

    public boolean contains(String variable) {
        return sessionStorage.containsKey(variable.toLowerCase());
    }

    public Object get(String variable) {
        return sessionStorage.get(variable.toLowerCase());
    }

    public long getId() {
        return this.sessionId;
    }

    public void set(final String s, final Object o) {
        sessionStorage.put(s.toLowerCase(), o);
    }
}
