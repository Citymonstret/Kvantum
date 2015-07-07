package com.intellectualsites.web.object;

public class CachedResponse {

    public final boolean isText;
    public final byte[] bodyBytes, headerBytes;

    public CachedResponse(final Response parent) {
        this.isText = parent.isText();
        this.headerBytes = parent.getHeader().getBytes();
        if (parent.isText()) {
            this.bodyBytes = parent.getContent().getBytes();
        } else {
            this.bodyBytes = parent.getBytes();
        }
    }

}
