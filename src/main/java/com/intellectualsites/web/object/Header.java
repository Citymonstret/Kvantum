package com.intellectualsites.web.object;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Header {

    private Map<String, String> headers;
    private String status;

    public Header(final String status) {
        this.status = status;
        this.headers = new HashMap<String, String>();
    }

    public Header set(final String key, final String value) {
        this.headers.put(key, value);
        return this;
    }

    public void apply(final PrintWriter out) {
        // We need to show that we are sending real content! :D
        out.println("HTTP/1.1 " + this.status);
        for (final Map.Entry<String, String> entry : this.headers.entrySet()) {
            out.println(entry.getKey() + ": " + entry.getValue());
        }
        // Print one empty line to indicate that the header sending is finished, this is important as the content would otherwise
        // be classed as headers, which really isn't optimal <3
        out.println();
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
        set("Set-Cookie", value);
    }

    public void removeCookie(final String cookie) {
        setCookie(cookie, "deleted; expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }

    public Header setStatus(final String status) {
        this.status = status;
        return this;
    }

}
