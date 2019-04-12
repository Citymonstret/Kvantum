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

import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.logging.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for common File operations (both NIO and IO types)
 */
@UtilityClass public final class FileUtils {

    /**
     * Copy a resource into a specified path, will use the system resource loader
     *
     * @param resourcePath Resource Path
     * @param path         Path where the resource should be pasted
     * @throws Exception All exceptions are thrown
     */
    public static void copyResource(final String resourcePath, final Path path) throws Exception {
        if (!Files.exists(path.getParent())) {
            Files.createDirectory(path.getParent());
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(ClassLoader.getSystemResourceAsStream(resourcePath)))) {
            String line;
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                while ((line = reader.readLine()) != null) {
                    writer.write(line + "\r\n");
                }
            }
        }
        Logger.info("Successfully copied '{0}' to '{1}'", resourcePath, path.getFileName());
    }

    /**
     * Add files to a zip file
     *
     * @param zipFile Zip File
     * @param files   Files to add to the zip
     * @throws Exception If anything goes wrong
     */
    public static void addToZip(final File zipFile, final File[] files)
        throws Exception {
        Assert.notNull(zipFile, files);

        final Map<String, String> env = new HashMap<>() {
            {
                put("create", "true");
            }
        };
        final Path path = zipFile.toPath();
        final URI uri = URI.create("jar:" + path.toUri());
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
            for (final File file : files) {
                Files.move(file.toPath(), fileSystem.getPath(file.getName()),
                    StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Attempt to create a folder. Logs a debug message if the folder couldn't be created
     *
     * @param folder Folder to create
     * @return Input (regardless if created or not)
     */
    public static File attemptFolderCreation(final File folder) {
        if (!folder.exists() && !folder.mkdirs()) {
            Message.COULD_NOT_CREATE_FOLDER.log(folder);
        }
        return folder;
    }

    /**
     * Copy a file from one location to another. Prints all exceptions.
     *
     * @param in   Ingoing File
     * @param out  Outgoing File
     * @param size Byte Buffer Size (in bytes)
     */
    public static void copyFile(final InputStream in, final OutputStream out,
        final int size) {
        Assert.isPositive(size); // Make sure that the buffer size is always a positive number
        try {
            final byte[] buffer = new byte[size];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

}
