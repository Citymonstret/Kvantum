/*
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
package com.github.intellectualsites.kvantum.implementation.config;

import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.config.YamlConfiguration;
import com.github.intellectualsites.kvantum.api.logging.LogModes;

import java.io.File;

final public class TranslationFile extends YamlConfiguration
{

    public TranslationFile(final File folder) throws Exception
    {
        super( "translations", new File( folder, "translations.yml" ) );
        this.loadFile();
        for ( final Message message : Message.values() )
        {
            final String nameSpace;
            switch ( message.getMode() )
            {
                case LogModes.MODE_DEBUG:
                    nameSpace = "debug";
                    break;
                case LogModes.MODE_INFO:
                    nameSpace = "info";
                    break;
                case LogModes.MODE_ERROR:
                    nameSpace = "error";
                    break;
                case LogModes.MODE_WARNING:
                    nameSpace = "warning";
                    break;
                default:
                    nameSpace = "info";
                    break;
            }
            this.setIfNotExists( nameSpace + "." + message.name().toLowerCase(), message.toString() );
        }
        this.saveFile();
    }

}
