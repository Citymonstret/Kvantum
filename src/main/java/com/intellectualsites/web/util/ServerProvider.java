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

package com.intellectualsites.web.util;

import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.syntax.VariableProvider;

final public class ServerProvider implements ProviderFactory<ServerProvider>, VariableProvider {

    public ServerProvider get(Request r) {
        return this;
    }

    public String providerName() {
        return "system";
    }

    public boolean contains(String variable) {
        switch(variable.toLowerCase()) {
            case "authors":
            case "filters":
            case "time":
            case "true":
            case "false":
            case "totalram":
            case "usedram":
            case "freeram":
                return true;
            default:
                return false;
        }
    }

    public Object get(String variable) {
        switch (variable.toLowerCase()) {
            case "time":
                return TimeUtil.getHTTPTimeStamp();
            case "authors":
                return new String[] { "Citymonstret", "IntellectualSites" };
            case "filters":
                return new String[] { "LIST", "UPPERCASE", "LOWERCASE", "JAVASCRIPT"};
            case "true":
                return true;
            case "false":
                return false;
            case "totalram":
                return (Runtime.getRuntime().totalMemory() / 1024) / 1024;
            case "usedram":
                return ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) / 1024;
            case "freeram":
                return (Runtime.getRuntime().freeMemory() / 1024) / 1024;
            default:
                return "";
        }
    }
}
