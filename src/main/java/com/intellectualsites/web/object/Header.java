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

package com.intellectualsites.web.object;

import com.intellectualsites.web.util.TimeUtil;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class Header {

    public static final String CONTENT_TYPE_CSS = "text/css; charset=utf-8";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream; charset=utf-8";
    public static final String CONTENT_TYPE_HTML = "text/html; charset=utf-8";
    public static final String CONTENT_TYPE_JAVASCRIPT = "text/javascript; charset=utf-8";
    public static final String CONTENT_TYPE_TEXT_EXAMPLE = "text/example; charset=utf-8";

    public static final String CACHE_NO_CACHE = "no-cache";

    public static final String POWERED_BY = "IntellectualServer";
    public static final String X_POWERED_BY = "Java/IntellectualServer 1.0";

    public static final String STATUS_TEMPORARY_REDIRECT = "307 Temporary Redirect";
    public static final String STATUS_OK = "200 OK";
    public static final String STATUS_NOT_ALLOWED = "405 Method not allowed";
    public static final String STATUS_CONTINUE = "100 Continue";
    public static final String STATUS_SWITCHING_PROTOCOLS = "101 Switching Protocols";
    public static final String STATUS_PROCESSING = "102 Processing";
    public static final String STATUS_CREATED = "201 Created";
    public static final String STATUS_ACCEPTED = "202 Accepted";
    public static final String STATUS_NON_AUTHORIATIVE_INFORMATION = "203 Non-Authoriative Information";
    public static final String STATUS_NO_CONTENT = "204 No Content";
    public static final String STATUS_RESET_CONTENT = "205 Reset Content";
    public static final String STATUS_PARTIAL_CONTENT = "206 Partial Content";

    public static final String ALLOW_ALL = "*";
    public static final String COOKIE_DELETED = "deleted; expires=Thu, 01 Jan 1970 00:00:00 GMT";

    /**
     * Specifying which web sites can
     * participate in cross-origin
     * resource sharing
     *
     * @see #ALLOW_ALL
     */
    public static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    /**
     * Specifies which patch document
     * formats this server supports
     */
    public static final String HEADER_ACCEPT_PATCH = "Accept-Patch";
    /**
     * What partial content range
     * types this server supports
     */
    public static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
    /**
     * The age the object has been in a
     * proxy cache, in seconds
     */
    public static final String HEADER_AGE = "Age";
    /**
     * Valid actions for a specified resource.
     * To be used for a 405 Method Not Allowed
     *
     * @see #STATUS_NOT_ALLOWED
     */
    public static final String HEADER_ALLOW = "Allow";
    /**
     * Tells all caching mechanism
     * from server to client, whether
     * they may cache this object. It
     * is measured in seconds
     */
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    /**
     * Control options for the current
     * connection and list of hop-by-hop
     * response fields
     */
    public static final String HEADER_CONNECTION = "Connection";
    /**
     * The type of encoding used on the data
     */
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    /**
     * The natural language, or languages
     * of the intended audience for the
     * enclosed content
     */
    public static final String HEADER_CONTENT_LANGUAGE = "Content-Language";
    public static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    /**
     * An opportunity to raise a "File
     * Download" dialogue box, for a
     * known MIME type with binary
     * format, or suggest a filename
     * for dynamic content. Quotes
     * are necessary with special
     * characters
     */
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    /**
     * Used in redirection, or when
     * a new resource has been created
     */
    public static final String HEADER_LOCATION = "Location";
    /**
     * A Base64-encoded binary MD5 sum
     * of the content of the response
     */
    public static final String HEADER_CONTENT_MD5 = "Content-MD5";
    /**
     * Where in a full body
     * message this partial message
     * belongs
     */
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    public static final String HEADER_X_POWERED_BY = "X-Powered-By";
    /**
     * The MIME type of this content
     */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    /**
     * CGI Heeader field specifying the
     * status of the HTTP response.
     * Normal HTTP response use
     * a Status-Line instead
     */
    public static final String HEADER_STATUS = "Status";
    /**
     * The date and time that the
     * message was sent (in HTTP-date format
     * as defined by RFC 7231
     * @see TimeUtil#getHTTPTimeStamp() To get a valid timestamp
     */
    public static final String HEADER_DATE = "Date";
    /**
     * An identifier for a specific
     * version of a resource, often
     * a message digest
     */
    public static final String HEADER_ETAG = "ETag";
    /**
     * A name for the server
     */
    public static final String HEADER_SERVER = "Server";
    /**
     * Gives the date/time after which
     * the response is considered stale
     * (in HTTP-date format as defined by
     * RFC 7231)
     *
     * @see TimeUtil#getHTTPTimeStamp() To get a valid timestamp
     */
    public static final String HEADER_EXPIRES = "Expires";
    /**
     * The last modified date for the
     * requested object (in HTTP-date
     * format as defined by RFC 7231)
     *
     * @see TimeUtil#getHTTPTimeStamp() To get a valid timestamp
     */
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    /**
     * Used to express a typed
     * relationship with another
     * resource, where the relation
     * type is defined by RFC 5988
     */
    public static final String HEADER_LINK = "Link";
    /**
     * The length of the response
     * body in octets (8-bit bytes)
     */
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    /**
     * Implementation-specific fields
     * that may have various effects
     * anywhere along the request-
     * response chain
     */
    public static final String HEADER_PRAGMA = "Pragma";
    /**
     * Request authentication to
     * access the proxy
     */
    public static final String HEADER_PROXY_AUTHENTICATE = "Proxy-Authenticate";
    /**
     * HTTP Public Key Pinning,
     * announces hash of a website's
     * authentic TLS certificate
     */
    public static final String HEADER_PUBLIC_KEY_PINS = "Public-Key-Pins";
    /**
     * Used in redirection, or
     * when a new resource has been
     * created.
     */
    public static final String HEADER_REFRESH = "Refresh";
    /**
     * If an enttity is temporarily
     * unavailable, this instructs
     * the client to try again later.
     * Value could be a specified
     * period of time (in seconds)
     * or a HTTP-date
     */
    public static final String HEADER_RETRY_AFTER = "Retry-After";

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
