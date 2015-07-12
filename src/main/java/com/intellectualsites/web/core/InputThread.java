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

package com.intellectualsites.web.core;

import com.intellectualsites.web.commands.CacheDump;
import com.intellectualsites.web.commands.Command;
import com.intellectualsites.web.commands.Show;
import com.intellectualsites.web.commands.Stop;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The thread which handles command inputs, when ran as a standalone
 * applications.
 *
 * @author Citymonstret
 */
@SuppressWarnings("all")
class InputThread extends Thread {

    private final Server server;
    private final Map<String, Command> commands;

    InputThread(Server server) {
        this.server = server;
        this.commands = new HashMap<>();

        this.commands.put("stop", new Stop());
        this.commands.put("cachedump", new CacheDump());
        this.commands.put("show", new Show());
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
            for (;;) {
                if ((line = in.readLine()).startsWith("/")) {
                    line = line.replace("/", "").toLowerCase();
                    String[] strings = line.split(" ");
                    String[] args;
                    if (strings.length > 1) {
                        args = new String[strings.length - 1];
                        System.arraycopy(strings, 1, args, 0, strings.length - 1);
                    } else {
                        args = new String[0];
                    }
                    String command = strings[0];
                    if (commands.containsKey(command)) {
                        commands.get(command).handle(args);
                    } else {
                        server.log("Unknown command '%s'", line);
                    }
                }
            }
        } catch (final Exception ignored) {
        }
    }
}
