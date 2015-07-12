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

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.views.View;
import com.intellectualsites.web.views.errors.View404;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class ViewManager {

    private final List<View> views;

    public ViewManager() {
        this.views = new ArrayList<>();
    }

    public void add(final View view) {
        Assert.notNull(view);

        for (View v : views) {
            if (v.toString().equalsIgnoreCase(view.toString())) {
                throw new IllegalArgumentException("Duplicate view pattern!");
            }
        }
        views.add(view);
    }

    public View match(final Request request) {
        for (View v : views) {
            if (v.matches(request)) {
                return v;
            }
        }
        return new View404();
    }

    public void dump(final Server server) {
        for (View view : views) {
            server.log("> View - Class '%s', Regex: '%s'\n\tOptions: %s", view.getClass().getSimpleName(), view.toString(), view.getOptionString());
        }
    }

    public void remove(View view) {
        if (views.contains(view)) {
            views.remove(view);
        }
    }
}
