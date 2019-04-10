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

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.io.KvantumInputStream;
import xyz.kvantum.server.api.io.KvantumOutputStream;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.RequestCompiler;
import xyz.kvantum.server.api.request.post.DummyPostRequest;
import xyz.kvantum.server.api.request.post.EntityType;
import xyz.kvantum.server.api.request.post.JsonPostRequest;
import xyz.kvantum.server.api.request.post.MultipartPostRequest;
import xyz.kvantum.server.api.request.post.RequestEntity;
import xyz.kvantum.server.api.request.post.UrlEncodedPostRequest;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.AutoCloseable;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Read a HTTP request until the first clear line. Does not read the HTTP message. The reader uses {@link
 * java.nio.charset.StandardCharsets#US_ASCII} as the charset, as defined by the HTTP protocol.
 */
@SuppressWarnings({"unused", "WeakerAccess"}) final class RequestReader {

    private static final AsciiString CONTENT_TYPE = AsciiString.of("content-type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.of("content-length");
    private static final AsciiString CONTENT_TYPE_URL_ENCODED =
        AsciiString.of("application/x-www-form-urlencoded");
    private static final AsciiString CONTENT_TYPE_MULTIPART = AsciiString.of("multipart");

    private final Object lock = new Object();
    @Getter private final StringBuilder builder;
    private final AbstractRequest abstractRequest;
    private char lastCharacter = ' ';
    private AtomicBoolean done = new AtomicBoolean(false);
    private boolean begunLastLine = false;
    private final AtomicBoolean hasQuery = new AtomicBoolean(false);
    private int contentLength = -1;
    @Getter private ReadTarget readTargetz = ReadTarget.REQUEST_HEADERS;
    @Getter private boolean cleared = true;
    private final WorkerContext context;

    // Request body
    private RequestOutputStream overflowStream;
    private RequestEntityReader requestEntityReader;

    RequestReader(final AbstractRequest abstractRequest, final WorkerContext workerContext) {
        this.abstractRequest = abstractRequest;
        this.context = workerContext;
        this.builder = new StringBuilder(CoreConfig.Limits.limitRequestLineSize);
    }

    ReadTarget getReadTarget() {
        synchronized (this.lock) {
            return this.readTargetz;
        }
    }

    void setReadTarget(final ReadTarget target) {
        synchronized (this.lock) {
            this.readTargetz = target;
        }
    }

    /**
     * Check whether the reader is done reading theabstractRequest
     *
     * @return true if the HTTP request is read, false if not
     */
    boolean isDone() {
        return this.done.get();
    }

    /**
     * Get the last read character, ' ' if no character has been read
     *
     * @return last read character
     */
    public char getLastCharacter() {
        return this.lastCharacter;
    }

    /**
     * Attempt to read a byte
     *
     * @param b Byte
     * @return True if the byte was read, False if not
     */
    boolean readByte(final byte b) throws Throwable {
        this.cleared = false;

        if (isDone() || getReadTarget() == ReadTarget.REQUEST_BODY) {
            return false;
        }

        final char character = (char) b;

        if (begunLastLine) {
            if (character == '\n') {
                final AsciiString contentLength = abstractRequest.getHeader(CONTENT_LENGTH);
                if (contentLength.isEmpty()) {
                    done.set(true);
                    return true;
                }
                try {
                    this.contentLength = contentLength.toInteger();
                } catch (final Exception e) {
                    throw new ReturnStatus(Header.STATUS_BAD_REQUEST, null, e);
                }
                if (this.contentLength >= CoreConfig.Limits.limitPostBasicSize) {
                    if (CoreConfig.debug) {
                        Logger.debug("Supplied post body size too large ({0} > {1})", contentLength,
                            CoreConfig.Limits.limitPostBasicSize);
                    }
                    throw new ReturnStatus(Header.STATUS_ENTITY_TOO_LARGE, null);
                }

                if (CoreConfig.debug && CoreConfig.verbose) {
                    Logger.debug("Creating a new request output stream for {}", this.abstractRequest);
                }

                this.setReadTarget(ReadTarget.REQUEST_BODY);
                this.overflowStream = new RequestOutputStream(this.contentLength);
                this.requestEntityReader = new RequestEntityReader(new KvantumInputStream(overflowStream, this.contentLength));
                // Submit the reading task
                ServerImplementation.getImplementation().getExecutorService().submit(this.requestEntityReader);
                return false; // Indicate that we should read into the stream instead
            } else {
                begunLastLine = false;
            }
        }

        if (lastCharacter == '\r') {
            if (character == '\n') {
                if (builder.length() != 0) {
                    final String line = builder.toString();
                    if (!this.hasQuery.get()) {
                        RequestCompiler.compileQuery(this.abstractRequest, line);
                        hasQuery.set(true);
                    } else {
                        final Optional<RequestCompiler.HeaderPair> headerPair =
                            RequestCompiler.compileHeader(line);
                        if (headerPair.isPresent()) {
                            final RequestCompiler.HeaderPair pair = headerPair.get();
                            this.abstractRequest.getHeaders().put(pair.getKey(), pair.getValue());
                        } else {
                            Logger.warn("Failed to read post request line: '{}'", line);
                        }
                    }
                }
                builder.setLength(0);
            }
        } else {
            if (lastCharacter == '\n' && character == '\r') {
                begunLastLine = true;
            } else if (character != '\n' && character != '\r') {
                builder.append(character);
            }
        }
        lastCharacter = character;
        return true;
    }

    /**
     * Attempt to read the integer as a byte, wrapper method for usage with {@link InputStream#read()}
     *
     * @param val Integer (byte)
     * @return true if the byte was read, false if not
     */
    boolean readByte(final int val) throws Throwable {
        if (val == -1) {
            this.done.set(true);
            return false;
        }
        return this.readByte((byte) val);
    }

    void readBytes(final ByteBuf byteBuf) throws Throwable {
        synchronized (this.lock) {
            if (this.getReadTarget() == ReadTarget.REQUEST_BODY && !this.overflowStream.canWrite()) {
                return; // Read nothing, we need to wait!
            }
            final int length = byteBuf.readableBytes();
            final byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes, 0, length);
            this.readBytes(bytes, length);
        }
    }

    /**
     * Attempt to read a byte array
     *
     * @param bytes  Byte array
     * @param length Length to be read
     * @return Number of read bytes
     */
    private int readBytes(final byte[] bytes, final int length) throws Throwable {
        synchronized (this.lock) {
            if (this.getReadTarget() == ReadTarget.REQUEST_HEADERS) {
                int read = 0;
                for (int i = 0; i < length && i < bytes.length; i++) {
                    if (this.readByte(bytes[i])) {
                        read++;
                    } else if (this.getReadTarget() == ReadTarget.REQUEST_BODY) {
                        // We need to copy the remaining data over
                        final int remaining = length - i;
                        if (CoreConfig.debug && CoreConfig.verbose) {
                            Logger.debug("Copying {0} bytes over to request entity (from {1})",
                                remaining, this.abstractRequest);
                        }
                        final byte[] remainingData = new byte[remaining];
                        System.arraycopy(bytes, i, remainingData, 0, remaining);
                        this.overflowStream.setBuffer(remainingData);

                        if (remainingData.length < (read + i)) {
                            throw new IllegalStateException(String.format("%d < %d", remainingData.length,
                                (read + i)));
                        }

                        return read + remainingData.length;
                    }
                    if (this.isDone()) {
                        break;
                    }
                }
                return read;
            } else {
                this.overflowStream.setBuffer(bytes);
            }
            return 0;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE) private class RequestEntityReader
        extends AutoCloseable implements Runnable {

        private final KvantumInputStream kvantumInputStream;

        @Override public void run() {
            if (isDone()) {
                if (CoreConfig.debug && CoreConfig.verbose) {
                    Logger.debug("RequestEntityReader started but terminated instantly, for request {}", abstractRequest);
                }
                return;
            }
            if (CoreConfig.debug && CoreConfig.verbose) {
                Logger.debug("RequestEntityReader reading (from {})", abstractRequest);
            }
            // Determine read strategy
            final AsciiString contentType = abstractRequest.getHeader(CONTENT_TYPE);
            final RequestEntity requestEntity;
            boolean isFormURLEncoded;
            if ((isFormURLEncoded = contentType.startsWith(CONTENT_TYPE_URL_ENCODED)) ||
                EntityType.JSON.getContentType().startsWith(contentType.toString())) {
                // Read into memory and then parse the request
                final StringBuilder builder = new StringBuilder(contentLength);
                try (final BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(this.kvantumInputStream))) {
                    if (CoreConfig.debug && CoreConfig.verbose) {
                        Logger.debug("RequestEntityReader reading data into memory (from {})", abstractRequest);
                    }
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        builder.append(line).append("\n");
                    }
                } catch (final IOException e) {
                    Logger.error("Failed to read request entity");
                    e.printStackTrace();
                }
                if (isFormURLEncoded) {
                    requestEntity = new UrlEncodedPostRequest(abstractRequest, builder.toString());
                } else {
                    requestEntity = new JsonPostRequest(abstractRequest, builder.toString());
                }
            } else if (contentType.startsWith(CONTENT_TYPE_MULTIPART)) {
                if (CoreConfig.debug && CoreConfig.verbose) {
                    Logger.debug("Creating a new multipart post request (for {})", abstractRequest);
                }
                // It's a multipart form
                requestEntity = new MultipartPostRequest(abstractRequest, this.kvantumInputStream);
                requestEntity.load();
            } else {
                Logger.warn("Request provided unknown post request type (Request: {0}): {1}",
                    abstractRequest, contentType);
                requestEntity = new DummyPostRequest(abstractRequest, "");
            }
            abstractRequest.setPostRequest(requestEntity);
            done.set(true);

            if (CoreConfig.debug && CoreConfig.verbose) {
                Logger.debug("Completely read request entity (from {})",  abstractRequest);
                Logger.debug("Read a total of {}B", this.kvantumInputStream.getTotalRead());
            }

            // We don't know if it's finished or not
            context.handleReadCompletion();
        }

        @Override protected void handleClose() {
            try {
                this.kvantumInputStream.close();
            } catch (final IOException ignored) {
            }
        }
    }

    private class RequestOutputStream extends KvantumOutputStream {

        private final Object lock = new Object();
        private final int expectedSize;

        @Getter private volatile byte[] buffer;
        private int read = 0;

        public RequestOutputStream(final int expectedSize) {
            this.expectedSize = expectedSize;
        }

        public void setBuffer(final byte[] buffer) {
            synchronized (this.lock) {
                if (this.isFinished()) {
                    throw new IllegalStateException("Cannot write when the stream is finished");
                } else if (!this.canWrite()) {
                    throw new IllegalStateException("Cannot write when the stream is being read");
                }
                this.buffer = buffer;
                this.read = 0;
            }
        }

        @Override public int read(@Nonnull byte[] buffer) {
            synchronized (this.lock) {
                if (this.isFinished()) {
                    return -1;
                }
                if (this.buffer == null || this.buffer.length == 0) {
                    return 0;
                }
                int toRead = Math.min(this.buffer.length, buffer.length);
                System.arraycopy(this.buffer, read, buffer, 0, toRead);
                this.read += toRead;
                if (this.expectedSize <= this.read) {
                    this.finish();
                }
                return toRead;
            }
        }

        @Override public int getOffer() {
            synchronized (this.lock) {
                return this.expectedSize - this.read; // this.buffer != null ? this.buffer.length - read : 0;
            }
        }

        public boolean canWrite() {
            synchronized (this.lock) {
                return this.buffer == null || this.buffer.length - this.read <= 0;// this.getOffer() <= 0;
            }
        }

    }

    private enum ReadTarget {
        REQUEST_HEADERS, REQUEST_BODY
    }

}
