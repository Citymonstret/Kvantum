/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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

import xyz.kvantum.files.CachedFile;
import xyz.kvantum.files.FileCacheManager;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.config.CoreConfig.Cache;
import xyz.kvantum.server.api.core.ServerImplementation;

import java.util.Optional;

final class FileCacheImplementation implements FileCacheManager {

    @Override public Optional<CachedFile> readCachedFile(final Path path) {
        return ServerImplementation.getImplementation().getCacheManager().getCachedFile(path);
    }

    @Override public void writeCachedFile(final Path path, final CachedFile content) {
        if (content.getLength() > Cache.cachedFilesMaxSize) {
            return;
        }
        ServerImplementation.getImplementation().getCacheManager().setCachedFile(path, content);
    }
}
