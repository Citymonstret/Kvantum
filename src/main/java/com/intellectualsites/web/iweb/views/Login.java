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

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.iweb.core.IWeb;
import com.intellectualsites.web.extra.accounts.Account;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.FileUtils;
import com.intellectualsites.web.views.View;

import java.io.File;
import java.util.regex.Matcher;

public class Login extends View {

    final String document;

    // final String form =
    //         "<form action=\"#\" method=\"post\">" +
    //             "<input type=\"hidden\" name=\"action\" value=\"login\">" +
    //             "<label for=\"username\">Username</label>" +
    //            "<input type=\"text\" name=\"username\" id=\"username\" placeholder=\"Username\">" +
    //            "<label for=\"password\">Password</label>" +
    //            "<input type=\"password\" name=\"password\" id=\"password\" placeholder=\"*******\">" +
    //            "<button type=\"submit\">Submit</button>" +
    //         "</form>";

    public Login() {
        super("\\/?(login)([\\s\\S]*)", "ilogin");
        document = FileUtils.getDocument(new File(new File(new File(Server.getInstance().coreFolder, "views"), "html"), "login.html"), getBuffer());
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        return true;
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);

        Account account = IWeb.getInstance().getAccountManager().getAccount(r.getSession());
        if (account == null) {
            if (r.getPostRequest().contains("action")) {
                if (r.getPostRequest().get("action").equalsIgnoreCase("login")) {
                    String username = r.getPostRequest().get("username");
                    byte[] password = r.getPostRequest().get("password").getBytes();

                    account = IWeb.getInstance().getAccountManager().getAccount(new Object[] {null, username});
                    if (account == null || !account.passwordMatches(password)) {
                         // response.setContent("Failed to login :(\n" + form);
                        r.addMeta("doc.error", "Failed to login");
                    } else {
                        IWeb.getInstance().getAccountManager().bindAccount(r.getSession(), account);
                        response.getHeader().redirect("./main");
                    }
                }
            } else {
                r.addMeta("doc.error", "");
            }
            response.setContent(FileUtils.getDocument(new File(new File(new File(Server.getInstance().coreFolder, "views"), "html"), "login.html"), getBuffer()));
        } else {
            response.getHeader().redirect("./main");
        }

        return response;
    }

}
