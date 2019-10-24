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
package xyz.kvantum.server.api.views;

import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.CoreConfig.Buffer;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.io.KvantumOutputStream;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.matching.FilePattern;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.FileExtension;
import xyz.kvantum.server.api.util.TimeUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings({"WeakerAccess", "unused"}) public abstract class StaticFileView extends View {

    final Collection<FileExtension> extensionList;

    public StaticFileView(String filter, Map<String, Object> options, String name,
        Collection<FileExtension> extensions) {
        super(filter, name, options, HttpMethod.ALL);
        this.extensionList = extensions;
    }

    @Override final public boolean passes(final AbstractRequest request) {
        final Map<String, String> variables = request.getVariables();
        FileExtension fileExtension;

        if (!variables.containsKey("extension")) {
            final Optional<String> extensionOptional = getOptionSafe("extension");
            extensionOptional.ifPresent(s -> variables.put("extension", s));
        }

        check:
        {
            for (final FileExtension extension : extensionList) {
                if (extension.matches(variables.get("extension"))) {
                    fileExtension = extension;
                    break check;
                }
            }
            Logger.error("Unknown file extension: " + variables.get("extension"));
            return false; // None matched
        }

        final FilePattern.FileMatcher fileMatcher = getFilePattern().matcher(() -> variables);
        request.addMeta("fileMatcher", fileMatcher);
        request.addMeta("extension", fileExtension);

        final Path file = getFile(request);
        request.addMeta("file", file);

        final boolean exists = file.exists();
        if (exists) {
            request.addMeta("file_length", file.length());
        }
        return exists;
    }

    @Override public void handle(final AbstractRequest r, final Response response) {
        final Object pathRaw = r.getMeta("file");
        if (pathRaw == null) {
            Logger.error("Encountered null \"file\" value in StaticFileView: {}", this.getName());
            return;
        }
        final Path path = (Path) pathRaw;
        final Object extensionRaw = r.getMeta("extension");
        if (extensionRaw == null) {
            Logger.error("Encountered null \"extension\" value in StaticFileView: {}",
                this.getName());
            return;
        }
        final FileExtension extension = (FileExtension) extensionRaw;
        response.getHeader().set(Header.HEADER_CONTENT_TYPE, extension.getContentType());
        final java.nio.file.Path javaPath = path.getJavaPath();

        final long fileLength = path.length();

        if (fileLength > CoreConfig.Buffer.files) // Large files won't be read into memory
        {
            if (CoreConfig.debug) {
                Logger.debug(
                    "Reading chunks of '{0}' (too big to store into primary memory: {1} > {2})",
                    path, fileLength, CoreConfig.Buffer.files);
            }
            final InputStream inputStream;
            final KvantumOutputStream responseStream = new KvantumOutputStream();
            try {
                inputStream = new FileInputStream(javaPath.toFile());
            } catch (final FileNotFoundException e) {
                ServerImplementation.getImplementation().getErrorDigest().digest(e);;
                return;
            }
            response.setResponse(responseStream);
            final byte[] outerBuffer = new byte[Buffer.files];
            final Consumer<Integer> writer = accepted -> {
                final int toRead = Math.min(outerBuffer.length, accepted);
                try {
                    final int read = inputStream.read(outerBuffer, 0, toRead);
                    if (CoreConfig.debug && read != toRead) {
                        Logger.debug("StaticFileView: accepted was {0} but {1} was read", read,
                            read);
                    }
                    responseStream.push(outerBuffer, read);
                } catch (final IOException e) {
                    ServerImplementation.getImplementation().getErrorDigest().digest(e);
                }
            };

            final Consumer<Integer> finalizedAction = totalRead -> {
                final int leftToRead = (int) (fileLength - totalRead);
                if (leftToRead > 0) {
                    responseStream.offer((int) (fileLength - totalRead), writer,
                        null /* Will re-use this one */);
                } else {
                    responseStream.finish();
                    try {
                        inputStream.close();
                    } catch (final Exception e) {
                        Logger.error("Failed to close the input stream in StaticFileView: {}",
                            e.getMessage());
                    }
                }
            };
            responseStream.offer((int) fileLength, writer, finalizedAction);
        } else {
            if (CoreConfig.debug) {
                Logger.debug("Reading entire file '{0}' into memory ({1} < {2})", path, fileLength,
                    Buffer.files);
            }
            if (extension.getReadType() == FileExtension.ReadType.BYTES || !ServerImplementation
                .getImplementation().getProcedure().hasHandlers()) {
                if (CoreConfig.debug) {
                    Logger.debug("Serving {} using byte[]", this);
                }
                response.setResponse(path.readBytes());
            } else {
                response.setResponse(
                    extension.getComment("Served to you by Kvantum") + System.lineSeparator() + path
                        .readFile());
            }
        }

        response.getHeader().set(Header.HEADER_LAST_MODIFIED,
            TimeUtil.getHTTPTimeStamp(new Date(path.getLastModified())));
    }
}
