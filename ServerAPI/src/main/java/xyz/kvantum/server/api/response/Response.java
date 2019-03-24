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
package xyz.kvantum.server.api.response;

import lombok.Getter;
import lombok.NonNull;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.TimeUtil;
import xyz.kvantum.server.api.views.RequestHandler;

import java.nio.charset.StandardCharsets;

/**
 * The HTTP response, this includes all headers and the actual bytecode.
 * {@inheritDoc}
 */
@SuppressWarnings("unused") public class Response implements ResponseBody {

    private static final ResponseStream DEFAULT_RESPONSE_STREAM =
        new ImmutableResponseStream(new byte[0]);

    @Getter private Header header;
    private RequestHandler parent;
    @Getter private ResponseStream responseStream = DEFAULT_RESPONSE_STREAM;
    @Getter private boolean text = false;

    /**
     * Constructor
     *
     * @param parent The view that generated this response
     */
    public Response(final RequestHandler parent) {
        this.parent = parent;
        this.header =
            new Header(Header.STATUS_OK).set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML)
                .set(Header.HEADER_SERVER, Header.POWERED_BY)
                .set(Header.HEADER_DATE, TimeUtil.getHTTPTimeStamp())
                .set(Header.HEADER_STATUS, Header.STATUS_OK);
    }

    public Response() {
        this(null);
    }

    public void copyFrom(final Response handle) {
        this.header = handle.header;
        this.parent = handle.parent;
        this.responseStream = handle.responseStream;
        this.text = handle.text;
    }

    /**
     * Use raw bytes, rather than text
     *
     * @param bytes Bytes to send to the client
     */
    public Response setResponse(final byte[] bytes) {
        return this.setResponse(new SimpleResponseStream(bytes));
    }

    public Response setResponse(@NonNull final ResponseStream stream) {
        this.responseStream = stream;
        return this;
    }

    /**
     * Set the header file
     *
     * @param header Header file
     */
    public Response setHeader(final Header header) {
        this.header = Assert.notNull(header);
        return this;
    }

    /**
     * Set the text content
     *
     * @param content The string content
     * @see #setResponse(byte[]) to send raw bytes
     */
    public Response setResponse(final String content) {
        this.text = true;
        return this.setResponse(content.getBytes(StandardCharsets.UTF_8));
    }

    public Response setParent(final RequestHandler parent) {
        this.parent = Assert.notNull(parent);
        return this;
    }

    public boolean supportsGzip() {
        if (this.responseStream instanceof KnownLengthStream) {
            final KnownLengthStream knownLengthStream = (KnownLengthStream) this.responseStream;
            return knownLengthStream.getLength() != 0;
        }
        return true;
    }

    public boolean hasParent() {
        return this.parent != null;
    }
}
