/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.plugin;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The plugin description file
 *
 * @author Citymonstret
 */
public final class PluginFile
{

    public final String name;
    final String mainClass;
    public final String author;
    public final String version;

    /**
     * Constructor
     *
     * @param stream Stream with desc.json incoming
     * @throws Exception If anything bad happens
     */
    PluginFile(final InputStream stream, final Yaml yaml) throws Exception
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
