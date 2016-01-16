//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2016 IntellectualSites                                                                  /
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

package com.intellectualsites.web.core;

import com.intellectualsites.web.config.Message;
import com.intellectualsites.web.util.Assert;
import com.intellectualsites.web.views.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 1/14/2016 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Step_LoadViews extends StartupStep {

    Step_LoadViews() {
        super("LoadViews");
    }

    @Override
    void execute(Server scope) {
        scope.log(Message.LOADING_VIEWS);
        Map<String, Map<String, Object>> views = scope.configViews.get("views");
        Assert.notNull(views);
        for (final Map.Entry<String, Map<String, Object>> entry : views.entrySet()) {
            Map<String, Object> view = entry.getValue();
            String type = "html", filter = view.get("filter").toString();
            if (view.containsKey("type")) {
                type = view.get("type").toString();
            }
            Map<String, Object> options;
            if (view.containsKey("options")) {
                options = (HashMap<String, Object>) view.get("options");
            } else {
                options = new HashMap<>();
            }

            if (scope.viewBindings.containsKey(type.toLowerCase())) {
                Class<? extends View> vc = scope.viewBindings.get(type.toLowerCase());
                try {
                    View vv = vc.getDeclaredConstructor(String.class, Map.class).newInstance(filter, options);
                    scope.viewManager.add(vv);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
