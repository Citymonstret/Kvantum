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

package com.intellectualsites.web.isites;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.extra.ApplicationStructure;
import com.intellectualsites.web.extra.accounts.AccountCommand;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.decl.ViewDeclaration;
import com.intellectualsites.web.views.decl.ViewMatcher;
import com.intellectualsites.web.views.staticviews.StaticViewManager;

/**
 * Java implementation of IntellectualSites
 *
 * This will be way more efficient than the PHP version, and a hell
 * of a lot more fun to work with ;)
 */
public class Application extends ApplicationStructure implements ViewDeclaration {

    protected static Application application;

    public static Application getApplication() {
        if (application == null) {
            application = new Application();
        }
        return application;
    }

    public Application() {
        super("intellectualsites");
    }

    public void registerViews(Server server) {
        server.getViewManager().clear();
        try {
            StaticViewManager.generate(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        server.inputThread.commands.put("account", new AccountCommand(this));
    }

    @ViewMatcher(filter = "(\\/)?(index)?", name = "intellectualsitesindex", cache = true)
    public Response getIndex(final Request in) {
        Response response = new Response(null);
        response.setContent("Hello World xD");
        return response;
    }
}
