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

package com.intellectualsites.web.iweb.accounts;

import com.intellectualsites.web.commands.Command;
import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.iweb.core.IWeb;
import com.intellectualsites.web.object.Session;

import java.sql.SQLException;

public class AccountCommand extends Command {

    @Override
    public void handle(String[] args) {
        if (args.length < 1) {
            send("Available Subcommands: create, setpassword, logout");
        } else {
            switch(args[0].toLowerCase()) {
                case "create": {
                    if (args.length < 3) {
                        send("Syntax: account create [username] [password]");
                    } else {
                        String username = args[1];
                        byte[] password = args[2].getBytes();

                        if (IWeb.getInstance().getAccountManager().getAccount(new Object[] { null, username }) != null) {
                            send("There is already an account with that username!");
                        } else {
                            Account account = new Account(IWeb.getInstance().getAccountManager().getNextId(), username, password);
                            send("Account created (Username: " + username + ", ID: " + account.getID() + ")");
                            try {
                                IWeb.getInstance().getAccountManager().createAccount(account);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } break;
                case "setpassword": {
                    if (args.length < 3) {
                        send("Syntax: account setpassword [username] [new password]");
                    }
                } break;
                case "logout": {
                    if (args.length < 2) {
                        send("Syntax: account logout [username]");
                    } else {
                        Account account = IWeb.getInstance().getAccountManager().getAccount(new Object[] {null, args[1]});
                        if (account == null) {
                            send("There is no such account");
                        } else {
                            Session session = IWeb.getInstance().getAccountManager().getSession(account);
                            if (session == null) {
                                send("There is no such session");
                            } else {
                                IWeb.getInstance().getAccountManager().unbindAccount(session);
                                send("Session unbound!");
                            }
                        }
                    }
                } break;
                default:
                    send("Unknown subcommand: " + args[0].toLowerCase());
                    break;
            }
        }
    }

    void send(String s) {
        Server.getInstance().log(s);
    }
}
