package com.intellectualsites.web.object;

import com.intellectualsites.web.util.TimeUtil;

/**
 * Created 2015-04-21 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Response {

    private Header header;
    private String content;
    private final View parent;

    public Response(final View parent) {
        this.parent = parent;
        this.header = new Header("200 OK")
                .set("Content-Type", "text/html")
                .set("Server", "IntellectualServer")
                .set("Date", TimeUtil.getHTTPTimeStamp())
                .set("Status", "200 OK")
                .set("X-Powered-By", "Java/IntellectualServer 1.0");
        this.content = "";
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setHeader(final Header header) {
        this.header = header;
    }

    public Header getHeader() {
        return this.header;
    }

    public String getContent() {
        return this.content;
    }
}
