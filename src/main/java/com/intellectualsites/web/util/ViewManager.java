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
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public void add(@NonNull final View view) {
        final Optional<View> illegalView = LambdaUtil.getFirst(views, v -> v.toString().equalsIgnoreCase(view.toString()));
        if (illegalView.isPresent()) {
            throw new IllegalArgumentException("Duplicate view pattern!");
        }
        views.add(view);
    }

    public View match(@NonNull final Request request) {
        final Optional<View> view = LambdaUtil.getFirst(views, request.matches);
        if (view.isPresent()) {
            return view.get();
        }
        return new View404(request.getQuery().getResource());
    }

    public void dump(@NonNull final Server server) {
        ((IConsumer<View>) view -> server.log("> View - Class '%s', Regex: '%s'\n\tOptions: %s",
                view.getClass().getSimpleName(), view.toString(), view.getOptionString())).foreach(views);
    }

    public void remove(@NonNull View view) {
        if (views.contains(view)) {
            views.remove(view);
        }
    }

    public void clear() {
        this.views.clear();
        Server.getInstance().log("Cleared views.");
    }
}
