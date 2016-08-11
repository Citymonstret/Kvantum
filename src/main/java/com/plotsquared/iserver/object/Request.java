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

package com.plotsquared.iserver.object;

import com.plotsquared.iserver.config.Message;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.syntax.ProviderFactory;
import com.plotsquared.iserver.object.syntax.VariableProvider;
import com.plotsquared.iserver.util.*;
import com.plotsquared.iserver.views.RequestHandler;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The HTTP Request Class
 * <p>
 * This is generated when a client
 * connects to the web server, and
 * contains the information needed
 * for the server to generate a
 * proper response. This is what
 * everything is based around!
 *
 * @author Citymonstret
 */
final public class Request implements ProviderFactory<Request>, VariableProvider, Validatable {

    final public Predicate<RequestHandler> matches = view -> view.matches(this);
    public Map<String, String> postponedCookies = new HashMap<>();
    private Map<String, Object> meta;
    private Map<String, String> headers;
    private Cookie[] cookies;
    private Query query;
    private PostRequest postRequest;
    private Socket socket;
    private Session session;
    private boolean valid = true;

    private Request() {
    }

    /**
     * The request constructor
     *
     * @param request Request (from the client)
     * @param socket  The socket which sent the request
     * @throws RuntimeException if the request doesn't contain a query
     */
    public Request(final String request, final Socket socket) {
        Assert.notNull(request, socket);

        this.socket = socket;
        this.headers = new HashMap<>();
        final String[] parts = request.split("\\|");
        for (final String part : parts) {
            final String[] subParts = part.split(":");
            if (subParts.length < 2) {
                if (headers.containsKey("query")) {
                    // This fixes issues with Nginx and proxy_pass
                    continue;
                }
                if (Server.getInstance().verbose) {
                    Server.getInstance().log("Query: " + subParts[0]);
                }
                headers.put("query", subParts[0]);
            } else {
                headers.put(subParts[0], subParts[1]);
            }
        }
        if (!this.headers.containsKey("query")) {
            throw new RuntimeException("Couldn't find query header...");
        }
        this.getResourceRequest();
        this.cookies = CookieManager.getCookies(this);
        this.meta = new HashMap<>();
    }

    public void removeMeta(String internalRedirect) {
        this.meta.remove(internalRedirect);
    }

    @Override
    public Request get(Request r) {
        return this;
    }

    @Override
    public String providerName() {
        return null;
    }

    @Override
    public boolean contains(String variable) {
        Assert.notNull(variable);

        return getVariables().containsKey(variable);
    }

    @Override
    public Object get(String variable) {
        Assert.notNull(variable);

        return getVariables().get(variable);
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Get the PostRequest
     *
     * @return PostRequest if exists, null if not
     */
    public PostRequest getPostRequest() {
        if (this.postRequest == null) {
            this.postRequest = new PostRequest("&");
        }
        return this.postRequest;
    }

    /**
     * The post request is basically... a POST request.
     *
     * @param postRequest The post request
     */
    public void setPostRequest(final PostRequest postRequest) {
        Assert.notNull(postRequest);

        this.postRequest = postRequest;
    }

    public Request newRequest(String query) {
        Assert.notEmpty(query);

        Request request = new Request();
        request.headers = new HashMap<>(headers);
        request.socket = socket;
        request.query = new Query(Method.GET, query);
        request.meta = new HashMap<>(meta);
        request.cookies = cookies;
        return request;
    }

    /**
     * Get all request cookies
     *
     * @return Request cookies
     */
    public Cookie[] getCookies() {
        return this.cookies;
    }

    /**
     * Get a request header. These
     * are sent by the client, and
     * are not to be confused with the
     * response headers.
     *
     * @param name Header Name
     * @return The header value, if the header exists. Otherwise an empty string will be returned.
     */
    public String getHeader(final String name) {
        Assert.notNull(name);

        if (this.headers.containsKey(name)) {
            return this.headers.get(name);
        }
        return "";
    }

    public Query getResourceRequest() {
        if (this.query != null) {
            return getQuery();
        }
        final String[] parts = getHeader("query").split(" ");
        if (parts.length < 3) {
            this.query = new Query(Method.GET, "/");
        } else {
            this.query = new Query(parts[0].equalsIgnoreCase("GET") ? Method.GET : Method.POST, parts[1]);
        }
        return this.query;
    }

    /**
     * Get the built query
     *
     * @return Compiled query
     */
    public Query getQuery() {
        return this.query;
    }

    /**
     * Build a string for logging
     *
     * @return Compiled string
     */
    public String buildLog() {
        return "Request >\n\tAddress: " + socket.getRemoteSocketAddress().toString() +
                "\n\tUser Agent: " + getHeader("User-Agent") + "\n\tRequest String: " +
                getHeader("query") + "\n\tHost: " + getHeader("Host") + "\n\tQuery: " +
                this.query.buildLog() + (postRequest != null ? "\n\tPost: " + postRequest.buildLog() : "");
    }

    /**
     * Add a meta value, which can
     * be used to share an object
     * throughout the lifespan of
     * the request.
     *
     * @param name Key (which will be used to get the meta value)
     * @param var  Value (Any object will do)
     * @see #getMeta(String) To get the value
     */
    public void addMeta(final String name, final Object var) {
        Assert.notNull(name, var);

        meta.put(name, var);
    }

    final public void internalRedirect(final String url) {
        Assert.notNull(url);

        this.addMeta("internalRedirect", newRequest(url));
        Message.INTERNAL_REDIRECT.log(url);
    }

    /**
     * Get a meta value
     *
     * @param name The key
     * @return Meta value if exists, else null
     * @see #addMeta(String, Object) To set a meta value
     */
    public Object getMeta(final String name) {
        Assert.notNull(name);

        if (!meta.containsKey(name)) {
            return null;
        }
        return meta.get(name);
    }

    @Final
    final public Map<String, String> getVariables() {
        return (Map<String, String>) getMeta("variables");
    }

    /**
     * Get the internal session
     *
     * @return true|null
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Set the internal session
     *
     * @param session Session
     */
    public void setSession(final Session session) {
        Assert.notNull(session);

        this.session = session;
    }

    /**
     * The query, for example:
     * "http://localhost/query?example=this"
     */
    public static class Query {

        private final Method method;
        private final String resource;
        private final Map<String, String> parameters = new HashMap<>();

        /**
         * The query constructor
         *
         * @param method   Request Method
         * @param resource The requested resource
         */
        Query(Method method, String resource) {
            Assert.notNull(method, resource);

            this.method = method;
            if (resource.contains("?")) {
                final String[] parts = resource.split("\\?");
                final String[] subParts = parts[1].split("&");
                resource = parts[0];
                for (final String part : subParts) {
                    final String[] subSubParts = part.split("=");
                    this.parameters.put(subSubParts[0], subSubParts[1]);
                }
            }
            this.resource = resource;
        }

        public Method getMethod() {
            return this.method;
        }

        public String getResource() {
            return this.resource;
        }

        public Map<String, String> getParameters() {
            return this.parameters;
        }

        /**
         * Build a logging string... for logging?
         *
         * @return compiled string
         */
        String buildLog() {
            return "Query >\n\t\tMethod: " + method.toString() + "\n\t\tResource: " + resource;
        }

        public String getFullRequest() {
            final String parameters = StringUtil.join(getParameters(), "=", "&");
            return parameters.isEmpty() ? resource : resource + "?" + parameters;
        }

    }

}
