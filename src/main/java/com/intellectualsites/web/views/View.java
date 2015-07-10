package com.intellectualsites.web.views;

import com.intellectualsites.web.object.syntax.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.util.Context;

import java.io.File;
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
    private final String internalName;

    private int buffer = -1;
    protected String relatedFolderPath;
    private File folder;

    /**
     * The constructor (Without prestored options)
     *
     * @param pattern used to decide whether or not to use this view
     * @see View(String, Map) - This is an alternate constructor
     */
    public View(String pattern, String internalName) {
        this(pattern, internalName, null);
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
    public View(String pattern, String internalName, Map<String, Object> options) {
        if (options == null) {
            this.options = new HashMap<>();
        } else {
            this.options = options;
        }
        this.internalName = internalName;
        this.pattern = Pattern.compile(pattern);
        this.rawPattern = pattern;
    }

    public final String getName() {
        return this.internalName;
    }

    /**
     * Get the folder used
     * by this view, doesn't
     * have to be used
     *
     * @return File
     */
    protected File getFolder() {
        if (this.folder == null) {
            if (relatedFolderPath != null) {
                this.folder = new File(Context.coreFolder, relatedFolderPath);
            } else if (containsOption("folder")) {
                this.folder = new File(getOption("folder").toString());
            } else {
                this.folder = new File(Context.coreFolder, "/" + internalName);
            }
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    System.out.println("Couldn't create the " + internalName + " folder...");
                }
            }
        }
        return this.folder;
    }

    /**
     * Get the file buffer (if needed)
     *
     * @return file buffer
     */
    protected int getBuffer() {
        if (this.buffer == -1) {
            if (containsOption("buffer")) {
                this.buffer = getOption("buffer");
            } else {
                this.buffer = 1024 * 64; // 64kb
            }
        }
        return this.buffer;
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
