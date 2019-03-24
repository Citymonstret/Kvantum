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
package xyz.kvantum.server.api.cache;

import lombok.NonNull;
import lombok.ToString;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.response.*;

import java.util.UUID;

/**
 * {@inheritDoc}
 */
@ToString(of = "uuid") public final class CachedResponse implements ResponseBody {

    public final Header header;
    private final UUID uuid = UUID.randomUUID();
    private final SimpleResponseStream responseStream;
    private final boolean isText;
    private boolean supportsGzip;

    public CachedResponse(@NonNull final ResponseBody parent) {
        this.header = parent.getHeader();
        this.isText = parent.isText();
        this.supportsGzip = parent.supportsGzip();
        final ResponseStream responseStream = parent.getResponseStream();
        if (!(responseStream instanceof KnownLengthStream)) {
            throw new IllegalArgumentException(
                "Supplied parent does not have a known length response stream");
        }
        final KnownLengthStream knownLengthStream = (KnownLengthStream) responseStream;
        this.responseStream = new SimpleResponseStream(knownLengthStream.getAll());
    }

    @Override public Header getHeader() {
        return this.header;
    }

    @Override public ResponseStream getResponseStream() {
        if (CoreConfig.debug) {
            Logger.debug("Creating a new copy of response stream: {}", this);
        }
        // Always return a copy of the response stream
        return new SimpleResponseStream(responseStream.getBytes());
    }

    @Override public boolean supportsGzip() {
        return this.supportsGzip;
    }

    @Override public boolean isText() {
        return this.isText;
    }
}
