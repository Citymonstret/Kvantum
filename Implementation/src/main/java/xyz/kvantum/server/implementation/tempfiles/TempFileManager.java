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
package xyz.kvantum.server.implementation.tempfiles;

import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.AutoCloseable;
import xyz.kvantum.server.api.util.ITempFileManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final public class TempFileManager extends AutoCloseable implements ITempFileManager {

    private final Map<String, WeakReference<Path>> pathReferences = new HashMap<>();

    @Override public Optional<Path> createTempFile(final String name) {
        try {
            final Path path = Files.createTempFile("kvantum-tempfile-", null);
            path.toFile().deleteOnExit();
            this.pathReferences.put(name, new WeakReference<>(path));
            return Optional.of(path);
        } catch (final IOException e) {
            Logger.error("Failed to create temp file: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override public Optional<Path> getFile(final String name) {
        if (pathReferences.containsKey(name)) {
            final WeakReference<Path> reference = pathReferences.get(name);
            return Optional.ofNullable(reference.get());
        }
        return Optional.empty();
    }

    @Override public void clearTempFiles() {
        for (final WeakReference<Path> weakReference : this.pathReferences.values()) {
            final Path path;
            if ((path = weakReference.get()) != null) {
                try {
                    Files.delete(path);
                } catch (final IOException e) {
                    Logger.error("Failed to delete temp file [{}]: {}", path.getFileName(),
                        e.getMessage());
                }
            }
        }
    }

    @Override protected void handleClose() {
        this.clearTempFiles();
    }
}
