package com.intellectualsites.web.object;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Header {

    public static final String CONTENT_TYPE_CSS = "text/css; charset=utf-8";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream; charset=utf-8";
    public static final String CONTENT_TYPE_HTML = "text/html; charset=utf-8";
    public static final String CONTENT_TYPE_JAVASCRIPT = "text/javascript; charset=utf-8";

    public static final String POWERED_BY = "IntellectualServer";
    public static final String X_POWERED_BY = "Java/IntellectualServer 1.0";

    public static final String STATUS_TEMPORARY_REDIRECT = "307 Temporary Redirect";
    public static final String STATUS_OK = "200 OK";

    public static final String COOKIE_DELETED = "deleted; expires=Thu, 01 Jan 1970 00:00:00 GMT";

    public static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_LOCATION = "Location";
    public static final String HEADER_X_POWERED_BY = "X-Powered-By";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_STATUS = "Status";
    public static final String HEADER_DATE = "Date";
    public static final String HEADER_SERVER = "Server";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";

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
        set(Header.HEADER_LOCATION, newURL);
        set(Header.HEADER_STATUS, Header.STATUS_TEMPORARY_REDIRECT);
        setStatus(Header.STATUS_TEMPORARY_REDIRECT);
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
        setCookie(cookie, Header.COOKIE_DELETED);
    }

    public Header setStatus(final String status) {
        this.status = status;
        return this;
    }

}
