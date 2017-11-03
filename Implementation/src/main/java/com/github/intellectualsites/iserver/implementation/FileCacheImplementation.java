/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver.implementation;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.files.FileCacheManager;

import java.util.Optional;

public final class FileCacheImplementation implements FileCacheManager
{

    @Override
    public Optional<String> readCachedFile(final String string)
    {
        if ( !CoreConfig.Cache.enabled )
        {
            return Optional.empty();
        }
        return Server.getInstance().getCacheManager().getCachedFile( string );
    }

    @Override
    public void writeCachedFile(final String string, final String content)
    {
        if ( !CoreConfig.Cache.enabled )
        {
            return;
        }
        Server.getInstance().getCacheManager().setCachedFile( string, content );
    }
}
