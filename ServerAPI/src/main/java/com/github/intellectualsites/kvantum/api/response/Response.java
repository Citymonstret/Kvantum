/*
 *
 *    Copyright (C) 2017 IntellectualSites
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
package com.github.intellectualsites.kvantum.api.response;

import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.TimeUtil;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import lombok.Getter;

/**
 * The HTTP response,
 * this includes all headers
 * and the actual bytecode.
 *
 * @author Citymonstret
 */
@SuppressWarnings("unused")
public class Response implements ResponseBody
{

    @Getter
    private Header header;
    @Getter
    private String content;
    private RequestHandler parent;
    @Getter
    private boolean text;
    @Getter
    private byte[] bytes;

    /**
     * Constructor
     *
     * @param parent The view that generated this response
     */
    public Response(final RequestHandler parent)
    {
        this.parent = parent;
        this.header = new Header( Header.STATUS_OK )
                .set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML )
                .set( Header.HEADER_SERVER, Header.POWERED_BY )
                .set( Header.HEADER_DATE, TimeUtil.getHTTPTimeStamp() )
                .set( Header.HEADER_STATUS, Header.STATUS_OK )
                .set( Header.HEADER_X_POWERED_BY, Header.X_POWERED_BY );
        this.content = "";
        this.bytes = new byte[ 0 ];
    }

    public Response()
    {
        this( null );
    }

    public void copyFrom(final Response handle)
    {
        this.header = handle.header;
        this.content = handle.content;
        this.parent = handle.parent;
        this.text = handle.text;
        this.bytes = handle.bytes;
    }

    /**
     * Use raw bytes, rather than text
     *
     * @param bytes Bytes to send to the client
     */
    public void setBytes(final byte[] bytes)
    {
        this.bytes = Assert.notNull( bytes );
        this.text = false;
    }

    /**
     * Set the header file
     *
     * @param header Header file
     */
    public Response setHeader(final Header header)
    {
        this.header = Assert.notNull( header );
        return this;
    }

    /**
     * Set the text content
     *
     * @param content The string content
     * @see #setBytes(byte[]) to send raw bytes
     */
    public Response setContent(final String content)
    {
        this.content = Assert.notNull( content );
        this.text = true;
        return this;
    }

    public Response setParent(final RequestHandler parent)
    {
        this.parent = Assert.notNull( parent );
        return this;
    }

    public boolean hasParent()
    {
        return this.parent != null;
    }

}
