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
package xyz.kvantum.server.api.request.post;

import lombok.Getter;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.fileupload.KvantumFileUploadContext;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MultipartPostRequest extends RequestEntity {

    @Getter private KvantumFileUploadContext.KvantumFileUploadContextParsingResult parsingResult;
    private final InputStream inputStream;

    public MultipartPostRequest(final AbstractRequest parent, final InputStream inputStream) {
        super(parent, "", true);
        this.inputStream = inputStream;
    }

    @Override protected void parseRequest(final String ignored) {
        this.parsingResult = KvantumFileUploadContext.from(this.getParent(), this.inputStream);
        if (parsingResult.getStatus()
            == KvantumFileUploadContext.KvantumFileUploadContextParsingStatus.SUCCESS) {
            final KvantumFileUploadContext context = this.parsingResult.getContext();
            try {
                final FileItemIterator itemIterator =
                    ServerImplementation.getImplementation().getGlobalFileUpload()
                        .getItemIterator(context);
                FileItemStream item;
                while (itemIterator.hasNext()) {
                    item = itemIterator.next();
                    try (final InputStream inputStream = item.openStream()) {
                        if (item.isFormField()) {
                            final List<String> lines = new ArrayList<>();
                            try (BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(inputStream))) {
                                String line;
                                while ((line = bufferedReader.readLine()) != null) {
                                    lines.add(line);
                                }
                            }
                            if (lines.size() != 1) {
                                Logger.warn("FileItem simple field line count is not 0 (Request: {})",
                                    getParent());
                                continue;
                            }
                            this.getVariables().put(item.getFieldName(), lines.get(0));
                        } else {
                            if (CoreConfig.debug && CoreConfig.verbose) {
                                Logger.info("Found file in multipart form from {0}, with field name {1}", this.getParent(), item.getFieldName());
                            }
                            final Optional<Path> tempFile = this.getParent().getTempFileManager().createTempFile(item.getName());
                            if (!tempFile.isPresent()) {
                                if (CoreConfig.debug && CoreConfig.verbose) {
                                    Logger.info("Could not create temp file, skipping file...");
                                }
                            } else {
                                final Path path = tempFile.get();
                                try (final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE)) {
                                    int data;
                                    while ((data = inputStream.read()) != -1) {
                                        outputStream.write(data);
                                    }
                                }
                                if (CoreConfig.debug && CoreConfig.verbose) {
                                    Logger.info("Write file with name {0} from field {1} for request {2}",
                                        item.getName(), item.getFieldName(), this.getParent());
                                }
                            }
                        }
                    }
                }
            } catch (final FileUploadException | IOException e) {
                e.printStackTrace();
            }
        } else {
            Logger.warn("Failed to parse multipart request: {}", parsingResult.getStatus());
        }
    }

    @Override public EntityType getEntityType() {
        return EntityType.FORM_MULTIPART;
    }
}
