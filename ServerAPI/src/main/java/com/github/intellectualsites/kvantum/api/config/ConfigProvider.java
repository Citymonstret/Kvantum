/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.config;

import com.github.intellectualsites.kvantum.api.util.Assert;
import lombok.EqualsAndHashCode;

/**
 * This is the configuration file that allows
 * us to access configuration file variables
 *
 * @author Citymonstret
 */
@EqualsAndHashCode(of = "name")
public abstract class ConfigProvider implements ConfigurationFile
{

    private final String name;

    /**
     * ConfigurationProvider Constructor
     *
     * @param name Configuration file name
     */
    public ConfigProvider(final String name)
    {
        Assert.notEmpty( name );

        this.name = name;
        ConfigVariableProvider.getInstance().add( this );
    }

    @Override
    public String toString()
    {
        return this.name;
    }

}
