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

import com.intellectualsites.web.object.LogWrapper;
import com.intellectualsites.web.util.TimeUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a booster, I.E, it's the strap on a boot.
 */
public class Bootstrap {

    private static Map<String, String> getOptions(final String[] args) {
        Map<String, String> out = new HashMap<>();
        for (final String arg : args) {
            String[] parts = arg.split("=");
            if (parts.length < 2) {
                out.put(parts[0].toLowerCase(), null);
            } else {
                out.put(parts[0].toLowerCase(), parts[1]);
            }
        }
        return out;
    }

    /**
     * Launcher method
     * @param args arguments
     */
    public static void main(String[] args) {
        Map<String, String> options = getOptions(args);
        File file;
        if (options.containsKey("folder")) {
            // folder=./this/new/path
            // folder=/web/intellectualserver/
            // and etc.
            file = new File(options.get("folder"));
        } else {
            file = new File("./");
        }
        startServer(true, file, new LogWrapper() {
            @Override
            public void log(String prefix, String prefix1, String timeStamp, String message) {
                System.out.printf("[%s][%s][%s] %s%s", prefix, prefix1, timeStamp, message, System.lineSeparator());
            }

            @Override
            public void log(String s) {
                System.out.println(s);
            }
        });
    }

    public static Server createServer(boolean standalone, File coreFolder, LogWrapper wrapper) {
        Server server = null;
        try {
            server = new Server(standalone, coreFolder, wrapper);
        } catch(final Exception e) {
            e.printStackTrace();
        }
        return server;
    }

    /**
     * Start a server, and get the instance
     *
     * @param standalone Should it run as a standalone application, or be integrated
     * @return the started server | null
     */
    public static Server startServer(boolean standalone, File coreFolder, LogWrapper wrapper) {
        Server server = null;
        try {
            server = createServer(standalone, coreFolder, wrapper);
            server.start();
        } catch(final Exception e) {
            e.printStackTrace();
        }
        return server;
    }
}
