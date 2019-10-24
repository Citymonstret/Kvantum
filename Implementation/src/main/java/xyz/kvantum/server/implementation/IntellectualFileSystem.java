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

import xyz.kvantum.files.FileSystem;
import xyz.kvantum.files.FileWatcher;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;

import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

final class IntellectualFileSystem extends FileSystem {

    IntellectualFileSystem(final java.nio.file.Path coreFolder) {
        super(coreFolder, new FileCacheImplementation());
    }

    void registerFileWatcher() {
        this.actOnSubPaths(this.getPath(""));
    }

    private void actOnSubPaths(final Path path) {
        final Collection<Path> subPaths = path.getSubPaths().stream().filter(Path::isFolder)
            .filter(p -> !Arrays.asList("log", "config", "storage").contains(p.getEntityName()))
            .collect(Collectors.toList());
        subPaths.forEach(this::registerCacheWatcher);
        subPaths.forEach(this::actOnSubPaths);
    }

    private void registerCacheWatcher(final Path path) {
        if (CoreConfig.debug) {
            Logger.debug("Registering cache invalidation watcher for: {}", path.getEntityName());
        }
        final FileWatcher fileWatcher = ServerImplementation.getImplementation().getFileWatcher();
        try {
            path.registerWatcher(fileWatcher, this::eventListener);
        } catch (final IOException e) {
            ServerImplementation.getImplementation().getErrorDigest().digest(e);
        }
    }

    @SuppressWarnings("unused")
    private void eventListener(final Path path, final WatchEvent.Kind<?> eventKind) {
        //
        // Ignore file creation and temporary job files
        //
        if (StandardWatchEventKinds.ENTRY_CREATE.equals(eventKind) || path.toString()
            .contains("___jb_")) {
            return;
        }
        Logger.info("Removing cache entry for: {}", path);
        ServerImplementation.getImplementation().getCacheManager().removeFileCache(path);
    }
}
