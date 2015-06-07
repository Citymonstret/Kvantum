package com.intellectualsites.web.object;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is used to create Crush syntax
 * such as {{variable.name}} or the foreach loop.
 * <p>
 * This has to be registered by the server in order to be used
 *
 * @author Citymonstret
 */
public abstract class Syntax {

    private final Pattern pattern;

    /**
     * Constructor
     *
     * @param pattern The regex pattern used to match the code
     */
    public Syntax(final Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Process the input string
     *
     * @param in Code Input
     * @param matcher RegEx Matcher
     * @param r HTML Request
     * @param factories Provider Factories
     *
     * @see #handle(String, Request, Map) Simple wrapper
     *
     * @return Processed string
     */
    public abstract String process(String in, Matcher matcher, Request r, Map<String, ProviderFactory> factories);

    /**
     * A simple wrapper for the process method
     *
     * @see #process(String, Matcher, Request, Map) Wraps around this
     *
     * @param in String input
     * @param r HTML Request
     * @param factories Provider Factories
     *
     * @return Processed Input
     */
    public final String handle(String in, Request r, Map<String, ProviderFactory> factories) {
        return process(in, pattern.matcher(in), r, factories);
    }

    /**
     * Check if the regex pattern matches
     *
     * @param in Code Input
     * @return True if the regex matches
     */
    public final boolean matches(String in) {
        return pattern.matcher(in).matches();
    }
}
