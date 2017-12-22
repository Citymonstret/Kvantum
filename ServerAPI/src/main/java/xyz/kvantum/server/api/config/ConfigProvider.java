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
package xyz.kvantum.server.api.config;

import lombok.EqualsAndHashCode;
import xyz.kvantum.server.api.util.Assert;

/**
 * This is the configuration file that allows
 * us to access configuration file variables
 *
 * @author Citymonstret
 */
@EqualsAndHashCode(of = "name")
@SuppressWarnings("WeakerAccess")
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
