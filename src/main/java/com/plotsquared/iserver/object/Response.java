//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.object;

import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.TimeUtil;
import com.plotsquared.iserver.views.RequestHandler;

/**
 * The HTTP response,
 * this includes all headers
 * and the actual bytecode.
 *
 * @author Citymonstret
 */
public class Response implements ResponseBody {

    private Header header;
    private String content;
    private RequestHandler parent;
    private boolean isText;
    private byte[] bytes;

    /**
     * Constructor
     *
     * @param parent The view that generated this response
     */
    public Response(final RequestHandler parent) {
        this.parent = parent;
        this.header = new Header(Header.STATUS_OK)
                .set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML)
                .set(Header.HEADER_SERVER, Header.POWERED_BY)
                .set(Header.HEADER_DATE, TimeUtil.getHTTPTimeStamp())
                .set(Header.HEADER_STATUS, Header.STATUS_OK)
                .set(Header.HEADER_X_POWERED_BY, Header.X_POWERED_BY);
        this.content = "";
        this.bytes = new byte[0];
    }

    public Response() {
        this(null);
    }

    /**
     * Get the bytes
     *
     * @return bytes, if exists
     * @see #isText() Should be false for this to work
     */
    @Override
    public byte[] getBytes() {
        return this.bytes;
    }

    /**
     * Use raw bytes, rather than text
     *
     * @param bytes Bytes to send to the client
     */
    public void setBytes(final byte[] bytes) {
        Assert.notNull(bytes);

        this.bytes = bytes;
        this.isText = false;
    }

    /**
     * Get the response header
     *
     * @return the set response header
     * @see #setHeader(Header) - To set the header
     */
    @Override
    public Header getHeader() {
        return this.header;
    }

    /**
     * Set the header file
     *
     * @param header Header file
     */
    public Response setHeader(final Header header) {
        Assert.notNull(header);

        this.header = header;
        return this;
    }

    /**
     * Get the content as a string
     *
     * @return The string content
     * @see #isText() Should be true for this to work
     */
    @Override
    public String getContent() {
        return this.content;
    }

    /**
     * Set the text content
     *
     * @param content The string content
     * @see #setBytes(byte[]) to send raw bytes
     */
    public Response setContent(final String content) {
        Assert.notNull(content);

        this.content = content;
        this.isText = true;
        return this;
    }

    /**
     * Check if using raw bytes, or a string
     *
     * @return True if using String, false if using bytes
     * @see #setContent(String) To set the string content
     * @see #setBytes(byte[]) To set the byte content
     */
    @Override
    public boolean isText() {
        return isText;
    }

    public Response setParent(final RequestHandler parent) {
        Assert.notNull(parent);

        this.parent = parent;
        return this;
    }

    public boolean hasParent() {
        return this.parent != null;
    }
}
