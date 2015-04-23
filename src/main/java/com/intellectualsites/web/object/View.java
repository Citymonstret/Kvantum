package com.intellectualsites.web.object;

import java.util.HashMap;
import java.util.Map;
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
    private final Map<String, Object> options;

    public View(String pattern) {
        this(pattern, null);
    }

    public <T> T getOption(final String s) {
        return (T) options.get(s);
    }

    public String getOptionString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Object> e : options.entrySet()) {
            b.append(";").append(e.getKey()).append("=").append(e.getValue().toString());
        }
        return b.toString();
    }

    public boolean containsOption(final String s) {
        return options.containsKey(s);
    }

    public View(String pattern, Map<String, Object> options) {
        if (options == null) {
            this.options = new HashMap<>();
        } else {
            this.options = options;
        }
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

    public ProviderFactory getFactory(final Request r) {
        return null;
    }

}
