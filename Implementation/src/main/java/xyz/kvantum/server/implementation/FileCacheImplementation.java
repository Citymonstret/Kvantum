/*
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
package xyz.kvantum.server.implementation;

import xyz.kvantum.files.FileCacheManager;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.config.CoreConfig;

import java.util.Optional;

public final class FileCacheImplementation implements FileCacheManager
{

    @Override
    public Optional<String> readCachedFile(final Path path)
    {
        if ( !CoreConfig.Cache.enabled )
        {
            return Optional.empty();
        }
        return Server.getInstance().getCacheManager().getCachedFile( path );
    }

    @Override
    public void writeCachedFile(final Path path, final String content)
    {
        if ( !CoreConfig.Cache.enabled )
        {
            return;
        }
        Server.getInstance().getCacheManager().setCachedFile( path, content );
    }
}
