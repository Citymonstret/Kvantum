package com.intellectualsites.web.object;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Header {

    private Map<String, String> headers;
    private String status;
    private String format;

    public Header(final String status, final String format) {
        this.status = status;
        this.format = format;
        this.headers = new HashMap<>();
    }

    public Header(final String status) {
        this(status, "HTTP/1.1");
    }

    public Header set(final String key, final String value) {
        this.headers.put(key, value);
        return this;
    }

    public byte[] getBytes() {
        StringBuilder temporary = new StringBuilder();
        temporary.append(this.format).append(" ").append(this.status).append("\n");
        for (final Map.Entry<String, String> entry : this.headers.entrySet()) {
            temporary.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        temporary.append("\n");
        return temporary.toString().getBytes();
    }

    public void apply(final OutputStream out) {
        try {
            out.write((this.format + " " + this.status + "\n").getBytes());
            for (final Map.Entry<String, String> entry : this.headers.entrySet()) {
                out.write((entry.getKey() + ": " + entry.getValue() + "\n").getBytes());
            }
            // Print one empty line to indicate that the header sending is finished, this is important as the content would otherwise
            // be classed as headers, which really isn't optimal <3
            out.write("\n".getBytes());
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }

    public String[] dump() {
        String[] dump = new String[this.headers.size() + 1];
        dump[0] = "HTTP/1.1 " + this.status;

        int index = 1;
        for (final Map.Entry<String, String> entry : this.headers.entrySet()) {
            dump[index++] = entry.getKey() + ": " + entry.getValue();
        }

        return dump;
    }

    public void redirect(final String newURL) {
        set("Location", newURL);
        set("Status", "307 Temporary Redirect");
        setStatus("307 Temporary Redirect");
    }

    public void setCookie(final String cookie, final String value) {
        String v = "";
        if (this.headers.containsKey("Set-Cookie")) {
            v = this.headers.get("Set-Cookie") + "," + cookie + "=" + value;
        } else {
            v = cookie + "=" + value;
        }
        set("Set-Cookie", v);
    }

    public void removeCookie(final String cookie) {
        setCookie(cookie, "deleted; expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }

    public Header setStatus(final String status) {
        this.status = status;
        return this;
    }

}
