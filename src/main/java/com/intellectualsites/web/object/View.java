package com.intellectualsites.web.object;

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

    public Response generate(final Request r) {
        Response response = new Response(this);
        response.setContent("<h1>Content!</h1>");
        return response;
    }
}
