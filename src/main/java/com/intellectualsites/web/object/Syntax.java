package com.intellectualsites.web.object;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Syntax {

    private final Pattern pattern;

    public Syntax(final Pattern pattern) {
        this.pattern = pattern;
    }

    public abstract String process(String in, Matcher matcher, Request r, Map<String, ProviderFactory> factories);

    public final String handle(String in, Request r, Map<String, ProviderFactory> factories) {
        return process(in, pattern.matcher(in), r, factories);
    }

    public final boolean matches(String in) {
        return pattern.matcher(in).matches();
    }

}
