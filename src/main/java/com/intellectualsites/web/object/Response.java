package com.intellectualsites.web.object;

import com.intellectualsites.web.util.TimeUtil;

/**
 * The HTTP response,
 * this includes all headers
 * and the actual bytecode.
 *
 * @author Citymonstret
 */
public class Response {

    private Header header;
    private String content;
    public final View parent;
    private boolean isText;
    private byte[] bytes;

    /**
     * Constructor
     *
     * @param parent The view that generated this response
     */
    public Response(final View parent) {
        this.parent = parent;
        this.header = new Header(Header.STATUS_OK)
                .set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML)
                .set(Header.HEADER_SERVER, Header.POWERED_BY)
                .set(Header.HEADER_DATE, TimeUtil.getHTTPTimeStamp())
                .set(Header.HEADER_STATUS, Header.STATUS_OK)
                .set(Header.HEADER_CACHE_CONTROL, Header.CACHE_NO_CACHE)
                .set(Header.HEADER_X_POWERED_BY, Header.X_POWERED_BY);
        this.content = "";
        this.bytes = new byte[0];
    }

    /**
     * Use raw bytes, rather than text
     *
     * @param bytes Bytes to send to the client
     */
    public void setBytes(final byte[] bytes) {
        this.bytes = bytes;
        this.isText = false;
    }

    /**
     * Get the bytes
     *
     * @return bytes, if exists
     * @see #isText() Should be false for this to work
     */
    public byte[] getBytes() {
        return this.bytes;
    }

    /**
     * Set the text content
     *
     * @param content The string content
     * @see #setBytes(byte[]) to send raw bytes
     */
    public void setContent(final String content) {
        this.content = content;
        this.isText = true;
    }

    /**
     * Set the header file
     *
     * @param header Header file
     */
    public void setHeader(final Header header) {
        this.header = header;
    }

    /**
     * Get the response header
     *
     * @return the set response header
     * @see #setHeader(Header) - To set the header
     */
    public Header getHeader() {
        return this.header;
    }

    /**
     * Get the content as a string
     *
     * @return The string content
     * @see #isText() Should be true for this to work
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Check if using raw bytes, or a string
     *
     * @return True if using String, false if using bytes
     * @see #setContent(String) To set the string content
     * @see #setBytes(byte[]) To set the byte content
     */
    public boolean isText() {
        return isText;
    }
}
