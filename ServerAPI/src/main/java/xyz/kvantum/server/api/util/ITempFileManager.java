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
package xyz.kvantum.server.api.util;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Manages per-request temporary files
 */
@SuppressWarnings("unused") public interface ITempFileManager {

    /**
     * Create a new temporary file
     *
     * @param name file name
     * @return new temporary file, if it was successfully created
     */
    Optional<Path> createTempFile(final String name);

    /**
     * Attempt to get a file using the file name specified
     * when creating the temp file
     *
     * @param name file name
     * @return file, if it can be found
     */
    Optional<Path> getFile(final String name);

    /**
     * Delete all temporary files created in this manager
     */
    void clearTempFiles();

}
