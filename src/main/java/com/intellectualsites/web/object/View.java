package com.intellectualsites.web.object;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * These are the view management classes.
 * Essentially, these are how the server
 * is able to output anything at all!
 *
 * @author Citymonstret
 */
public abstract class View {

    private final Pattern pattern;
    private final String rawPattern;
    private final Map<String, Object> options;

    /**
     * The constructor (Without prestored options)
     *
     * @param pattern used to decide whether or not to use this view
     * @see View(String, Map) - This is an alternate constructor
     */
    public View(String pattern) {
        this(pattern, null);
    }

    /**
     * Get a stored option
     *
     * @param s   Key
     * @param <T> Type
     * @return (Type Casted) Value
     * @see #containsOption(String) Check if the option exists before getting it
     */
    @SuppressWarnings("ALL")
    public <T> T getOption(final String s) {
        return (T) options.get(s);
    }

    /**
     * Get all options as a string
     *
     * @return options as string
     */
    public String getOptionString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Object> e : options.entrySet()) {
            b.append(";").append(e.getKey()).append("=").append(e.getValue().toString());
        }
        return b.toString();
    }

    /**
     * Check if the option is stored
     *
     * @param s Key
     * @return True if the option is stored, False if it isn't
     */
    public boolean containsOption(final String s) {
        return options.containsKey(s);
    }

    /**
     * Constructor with prestored options
     *
     * @param pattern Regex pattern that will decide whether or not to use this view
     * @param options Pre Stored options
     */
    public View(String pattern, Map<String, Object> options) {
        if (options == null) {
            this.options = new HashMap<>();
        } else {
            this.options = options;
        }
        this.pattern = Pattern.compile(pattern);
        this.rawPattern = pattern;
    }

    /**
     * Check if the request URL matches the regex pattern
     *
     *
     * @param request Request, from which the URL should be checked
     *
     * @see #passes(Matcher, Request) - This is called!
     * @return True if the request Matches, False if not
     */
    public boolean matches(final Request request) {
        Matcher matcher = pattern.matcher(request.getQuery().getResource());
        return matcher.matches() && passes(matcher, request);
    }

    /**
     * This is for further testing (... further than regex...)
     * For example, check if a file exists etc.
     *
     * @param matcher The regex matcher
     * @param request The request from which the URL is fetches
     *
     * @return True if the request matches, false if not
     */
    public abstract boolean passes(Matcher matcher, Request request);

    @Override
    public String toString() {
        return this.rawPattern;
    }

    /**
     * Generate the response
     *
     * @param r Request
     * @return Generated response
     */
    public Response generate(final Request r) {
        Response response = new Response(this);
        response.setContent("<h1>Content!</h1>");
        return response;
    }

    /**
     * Get the view specific factory (if it exists)
     *
     * @param r Request IN
     * @return Null by default, or the ProviderFactory (if set by the view)
     */
    public ProviderFactory getFactory(final Request r) {
        return null;
    }

}
