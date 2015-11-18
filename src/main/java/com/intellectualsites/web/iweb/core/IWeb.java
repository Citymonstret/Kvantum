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

package com.intellectualsites.web.iweb.core;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.extra.ApplicationStructure;
import com.intellectualsites.web.extra.accounts.AccountCommand;
import com.intellectualsites.web.extra.accounts.AccountManager;
import com.intellectualsites.web.iweb.views.Login;
import com.intellectualsites.web.iweb.views.Main;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.SQLiteManager;
import com.intellectualsites.web.views.View;
import com.intellectualsites.web.views.ViewReturn;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;

/**
 * Created 10/24/2015 for IntellectualServer
 *
 * @author Citymonstret
 */
public class IWeb extends ApplicationStructure {

    protected static IWeb instance;

    public static IWeb getInstance() {
        if (instance == null) {
            instance = new IWeb();
        }
        return instance;
    }

    public IWeb() {
        super("iweb");
    }

    public void registerViews(Server server) {
        server.getViewManager().add(new Login());
        server.getViewManager().add(new Main());
        server.inputThread.commands.put("account", new AccountCommand(this));
    }

    {
        new View("", "", null, new ViewReturn() {
            @Override
            public Response get(Request r) {
                return new Response();
            }
        }).register();
    }
}
