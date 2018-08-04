/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.files;

import java.util.Optional;

/**
 * High-level manager for file caching
 */
public interface FileCacheManager
{

    /**
     * Read a file from the file cache if it exists in the cache
     *
     * @param path (Unique) File identifier
     * @return Optional file content
     */
    Optional<CachedFile> readCachedFile(Path path);

    /**
     * Write a file to the file cache
     *
     * @param path       (Unique) File identifier
     * @param cachedFile File content
     */
    void writeCachedFile(Path path, CachedFile cachedFile);

}
