package com.intellectualsites.web.object;

import com.intellectualsites.web.util.TimeUtil;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public abstract class View {

    private final Pattern pattern;
    private final String rawPattern;

    public View(String pattern) {
        this.pattern = Pattern.compile(pattern);
        this.rawPattern = pattern;
    }

    public boolean matches(final Request request) {
        Matcher matcher = pattern.matcher(request.getQuery().getResource());
        return matcher.matches() && passes(matcher, request);
    }

    public abstract boolean passes(Matcher matcher, Request request);

    @Override
    public String toString() {
        return this.rawPattern;
    }

    public void headers(final PrintWriter out, Request request) {
        out.println("HTTP/1.1 200 OK"); // HTTP Status Code http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
        out.println("Content-Type: text/html"); // Content Type
        out.println("Server: IntellectualServer"); // Server Name
        out.println("Date: " + TimeUtil.getHTTPTimeStamp()); // Date
        out.println("Status: 200 OK"); // Repeated status header
        out.println("X-Powered-By: Java/IntellectualServer 1.0");
        /*
         *  String cookie = request.getHeader("Cookie");
            if (cookie.equals("")) {
                out.println("Set-Cookie: session=potato");
            } else {
                System.out.println("cookie: " + cookie);
            }
         */
    }

    public void content(final PrintWriter out, Request r) {
        out.println("<h1>Content!</h1>");
    }
}
