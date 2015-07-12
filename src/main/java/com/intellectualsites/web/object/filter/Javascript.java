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

package com.intellectualsites.web.object.filter;

import com.intellectualsites.web.object.syntax.Filter;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created 2015-07-12 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Javascript extends Filter {

    public Javascript() {
        super("javascript");
    }

    public Object handle(String objectName, Object o) {
        StringBuilder s = new StringBuilder();
        s.append("var ").append(objectName).append(" = ");
        if (o instanceof Object[]) {
            Object[] oo = (Object[]) o;
            s.append("[\n");
            Iterator iterator = Arrays.asList(oo).iterator();
            while (iterator.hasNext()) {
                Object ooo = iterator.next();
                handleObject(s, ooo);
                if (iterator.hasNext()) {
                    s.append(",\n");
                }
            }
            s.append("]");
        } else {
            handleObject(s, o);
        }
        return s.append(";").toString();
    }

    private void handleObject(StringBuilder s, Object o) {
        if (o instanceof Number || o instanceof Boolean) {
            s.append(o);
        } else if (o instanceof Object[]) {
            for (Object oo : (Object[]) o) {
                handleObject(s, oo);
            }
        } else {
            s.append("\"").append(o.toString()).append("\"");
        }
    }
}
