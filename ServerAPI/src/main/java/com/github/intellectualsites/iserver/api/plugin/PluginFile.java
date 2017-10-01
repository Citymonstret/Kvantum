/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.plugin;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The plugin description file
 *
 * @author Citymonstret
 */
public class PluginFile
{

    public final String name;
    public final String mainClass;
    public final String author;
    public final String version;

    /**
     * Constructor
     *
     * @param stream Stream with desc.json incoming
     * @throws Exception If anything bad happens
     */
    public PluginFile(final InputStream stream, Yaml yaml) throws Exception
    {
        Map info;
        Object temp = yaml.load( stream );
        if ( temp instanceof Map )
        {
            info = (Map) temp;
        } else
        {
            info = new HashMap<>();
        }
        name = info.get( "name" ).toString();
        mainClass = info.get( "main" ).toString();
        author = info.get( "author" ).toString();
        version = info.get( "version" ).toString();
        stream.close();
    }

}
