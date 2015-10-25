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

package com.intellectualsites.web.iweb.views;

import com.intellectualsites.web.iweb.IWeb;
import com.intellectualsites.web.iweb.accounts.Account;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.View;

import java.util.regex.Matcher;

/**
 * Created 10/24/2015 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Main extends View {

    public Main() {
        super("\\/?(main)([\\s\\S]*)", "imain");
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        return true;
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);

        Account account = IWeb.getInstance().getAccountManager().getAccount(r.getSession());
        if (account != null) {
            response.setContent("Hello " + account);

            int parts = r.getQuery().getResource().split("/").length;
            if (parts > 2) {
                String action = r.getQuery().getResource().split("/")[2];
                response.setContent("Action: " + action);
                switch (action) {
                    case "logout": {
                        IWeb.getInstance().getAccountManager().unbindAccount(r.getSession());
                        response.getHeader().redirect("/login");
                    } break;
                    default: break;
                }
            }
        } else {
            response.getHeader().redirect("/login");
        }

        return response;
    }
}
