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
