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
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

final public class Session implements VariableProvider {

    private static long id = 0L;

    @Getter
    private long sessionId = 0;
    private final Map<String, Object> sessionStorage;

    public Session() {
        sessionStorage = new HashMap<>();
        sessionId = id++;
    }

    public boolean contains(@NonNull final String variable) {
        return sessionStorage.containsKey(variable.toLowerCase());
    }

    public Object get(@NonNull final String variable) {
        return sessionStorage.get(variable.toLowerCase());
    }

    public void set(@NonNull final String s, @NonNull final Object o) {
        sessionStorage.put(s.toLowerCase(), o);
    }

}
