package com.intellectualsites.web.object;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 2015-04-21 for IntellectualServer
 *
 * @author Citymonstret
 */
public class PostRequest {

    public String request;
    private Map<String, String> vars;

    public PostRequest(final String request) {
        this.request = request;
        this.vars = new HashMap<>();
        for (String s : request.split("&")) {
            String[] p = request.split("=");
            vars.put(p[0], p[1]);
        }
    }

    public String buildLog() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, String> e : vars.entrySet()) {
            b.append(e.getKey()).append("=").append(e.getValue()).append("&");
        }
        return b.toString();
    }

    public String get(String k) {
        return vars.get(k);
    }

    public boolean contains(String k) {
        return vars.containsKey(k);
    }

    public Map<String, String> get() {
        return vars;
    }
}
