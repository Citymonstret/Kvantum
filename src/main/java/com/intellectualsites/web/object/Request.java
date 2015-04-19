package com.intellectualsites.web.object;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Request {

    private Map<String, Object> meta;
    private final String host, userAgent, requestString, raw;
    private Query query;

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

    public Request(final String request) {
        this.raw = request;
        String[] parts = request.split("\\|");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Not enough parts (request|user-agent)");
        }
        this.requestString = parts[0];
        this.host = parts[1];
        this.userAgent = parts[2];
        getResourceRequest();

        this.meta = new HashMap<String, Object>();
    }

    private void getResourceRequest() {
        String[] parts = requestString.split(" ");
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
        return "Request >\n\tUser Agent: " + this.userAgent + "\n\tRequest String: " + requestString + "\n\tHost: " + this.host + "\n\tQuery: " + this.query.buildLog();
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
