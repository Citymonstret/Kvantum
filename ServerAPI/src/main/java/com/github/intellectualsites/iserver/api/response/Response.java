/*
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.response;

import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.api.util.TimeUtil;
import com.github.intellectualsites.iserver.api.views.RequestHandler;
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
