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

import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.views.View;

/**
 * Server API
 *
 * @deprecated
 */
@SuppressWarnings({"unused","deprecation"})
public class ServerAPI {

    private static ServerAPI instance;

    /**
     * Get the server api instance
     *
     * @return WebServer API instance
     */
    public static ServerAPI instance() {
        return instance;
    }

    protected static void setInstance(final Server server) {
        instance = new ServerAPI(server);
    }

    private final Server server;
    protected ServerAPI(final Server server) {
        this.server = server;
    }

    public void addView(final View view) {
        server.viewManager.add(view);
    }

    public void removeView(final View view) {
        server.viewManager.remove(view);
    }

    public void addProviderFactory(final ProviderFactory factory) {
        server.providers.add(factory);
    }
}
