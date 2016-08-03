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

package com.intellectualsites.web.object;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.util.CookieManager;
import com.intellectualsites.web.util.StringUtil;
import com.intellectualsites.web.views.View;
import lombok.Getter;
import lombok.NonNull;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The HTTP Request Class
 *
 * This is generated when a client
 * connects to the web server, and
 * contains the information needed
 * for the server to generate a
 * proper response. This is what
 * everything is based around!
 *
 * @author Citymonstret
 */
final public class Request {

    private Map<String, Object> meta;
    private final Map<String, String> headers;
    private Cookie[] cookies;
    private Query query;
    private PostRequest postRequest;
    private final Socket socket;
    private Session session;
    public Map<String, String> postponedCookies = new HashMap<>();

    /**
     * The post request is basically... a POST request.
     *
     * @param postRequest The post request
     */
    public void setPostRequest(@NonNull final PostRequest postRequest) {
        this.postRequest = postRequest;
    }

    /**
     * The query, for example:
     * "http://localhost/query?example=this"
     */
    public static class Query {

        @Getter
        private final Method method;
        @Getter
        private final String resource;
        @Getter
        private final Map<String, String> parameters = new HashMap<>();

        /**
         * The query constructor
         *
         * @param method   Request Method
         * @param resource The requested resource
         */
        Query(Method method, String resource) {
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
     * The request constructor
     *
     * @throws RuntimeException if the request doesn't contain a query
     *
     * @param request Request (from the client)
     * @param socket The socket which sent the request
     */
    public Request(@NonNull final String request, @NonNull final Socket socket) {
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
    public String getHeader(@NonNull final String name) {
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
        return "Request >\n\tAddress: " + socket.getRemoteSocketAddress().toString() + "\n\tUser Agent: " + getHeader("User-Agent") + "\n\tRequest String: " + getHeader("query") + "\n\tHost: " + getHeader("Host") + "\n\tQuery: " + this.query.buildLog() + (postRequest != null ? "\n\tPost: " + postRequest.buildLog() : "");
    }

    /**
     * Add a meta value, which can
     * be used to share an object
     * throughout the lifespan of
     * the request.
     *
     * @see #getMeta(String) To get the value
     *
     * @param name Key (which will be used to get the meta value)
     * @param var Value (Any object will do)
     */
    public void addMeta(@NonNull final String name, @NonNull final Object var) {
        meta.put(name, var);
    }

    /**
     * Get a meta value
     *
     * @see #addMeta(String, Object) To set a meta value
     *
     * @param name The key
     * @return Meta value if exists, else null
     */
    public Object getMeta(@NonNull final String name) {
        if (!meta.containsKey(name)) {
            return null;
        }
        return meta.get(name);
    }

    /**
     * Set the internal session
     *
     * @param session Session
     */
    public void setSession(@NonNull final Session session) {
        this.session = session;
    }

    /**
     * Get the internal session
     *
     * @return true|null
     */
    public Session getSession() {
        return this.session;
    }

    final public Predicate<View> matches = view -> view.matches(this);

}
