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

package com.plotsquared.iserver.config;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.object.syntax.ProviderFactory;
import com.plotsquared.iserver.object.syntax.VariableProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * This allows you to access configuration
 * variables through the variable syntax
 *
 * @author Citymonstret
 */
public class ConfigVariableProvider implements ProviderFactory<ConfigVariableProvider>, VariableProvider {

    private static ConfigVariableProvider instance;
    private final Map<String, ConfigProvider> configurations;

    private ConfigVariableProvider() {
        configurations = new HashMap<>();
    }

    public static ConfigVariableProvider getInstance() {
        if (instance == null) {
            instance = new ConfigVariableProvider();
        }
        return instance;
    }

    public void add(final ConfigProvider provider) {
        configurations.put(provider.toString(), provider);
    }

    @Override
    public ConfigVariableProvider get(Request r) {
        return this;
    }

    @Override
    public String providerName() {
        return "cfg";
    }

    @Override
    public boolean contains(String variable) {
        String[] parts = variable.split("@");
        return configurations.containsKey(parts[0]) && configurations.get(parts[0]).contains(parts[1]);
    }

    @Override
    public Object get(String variable) {
        String[] parts = variable.split("@");
        return configurations.get(parts[0]).get(parts[1]);
    }
}
