package com.intellectualsites.web.object;

import com.intellectualsites.web.util.CookieManager;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Request {

    private Map<String, Object> meta;
    private Map<String, String> headers;
    private Cookie[] cookies;
    private final String raw;
    private Query query;
    private PostRequest postRequest;
    private Socket socket;

    public static class Query {
        private final Method method;
        private final String resource;

        public Query(Method method, String resource) {
            this.method = method;
            this.resource = resource;
        }

        public Method getMethod() {
            return this.method;
        }

        public String getResource() {
            return this.resource;
        }

        public String buildLog() {
            return "Query >\n\t\tMethod: " + method.toString() + "\n\t\tResource: " + resource;
        }
    }

    public Request(final String request, final Socket socket) {
        this.raw = request;
        this.socket = socket;

        String[] parts = request.split("\\|");

        this.headers = new HashMap<String, String>();
        for (String part : parts) {
            String[] subParts = part.split(":");
            if (subParts.length < 2) {
                headers.put("query", subParts[0]);
            } else {
                headers.put(subParts[0], subParts[1]);
            }
        }

        if (!this.headers.containsKey("query")) {
            throw new RuntimeException("Couldn't find query header...");
        }

        getResourceRequest();

        this.cookies = CookieManager.getCookies(this);

        this.meta = new HashMap<String, Object>();
    }

    public Cookie[] getCookies() {
        return this.cookies;
    }

    public String getHeader(final String name) {
        if (this.headers.containsKey(name)) {
            return this.headers.get(name);
        }
        return "";
    }

    private void getResourceRequest() {
        String[] parts = getHeader("query").split(" ");
        if (parts.length < 3) {
            this.query = new Query(Method.GET, "/");
        } else {
            Method method = parts[0].equalsIgnoreCase("GET") ? Method.GET : Method.POST;
            this.query = new Query(method, parts[1]);
        }
    }

    public Query getQuery() {
        return this.query;
    }

    public String buildLog() {
        return "Request >\n\tAddress: " + socket.getRemoteSocketAddress().toString() + "\n\tUser Agent: " + getHeader("User-Agent") + "\n\tRequest String: " + getHeader("query") + "\n\tHost: " + getHeader("Host") + "\n\tQuery: " + this.query.buildLog();
    }

    public void addMeta(String name, Object var) {
        meta.put(name, var);
    }

    public Object getMeta(String name) {
        if (!meta.containsKey(name)) {
            return null;
        }
        return meta.get(name);
    }
}
