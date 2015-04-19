package com.intellectualsites.web.views.errors;

import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.View;
import com.intellectualsites.web.util.TimeUtil;

import java.io.PrintWriter;
import java.util.regex.Matcher;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Error extends View {

    private final int code;
    private final String desc;

    public Error(final int code, final String desc) {
        super("");
        this.code = code;
        this.desc = desc;
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        return true;
    }

    @Override
    public void headers(final PrintWriter out, Request request) {
        out.println(String.format("HTTP/1.1 %d %s", this.code, this.desc)); // HTTP Status Code http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
        out.println("Content-Type: text/html"); // Content Type
        out.println("Server: IntellectualServer"); // Server Name
        out.println("Date: " + TimeUtil.getHTTPTimeStamp()); // Date
        out.println(String.format("Status: %d %s", this.code, this.desc)); // Repeated status header
        out.println("X-Powered-By: Java/IntellectualServer 1.0");
    }

    @Override
    public void content(final PrintWriter out, final Request r) {
        out.println(String.format("<p><b>Error:</b> %d %s</p>", this.code, this.desc));
    }
}
