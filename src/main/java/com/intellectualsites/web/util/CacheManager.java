package com.intellectualsites.web.util;

import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.object.View;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    private final Map<String, Response> cachedResponses;

    public CacheManager() {
        this.cachedResponses = new ConcurrentHashMap<>();
    }

    public boolean hasCache(View view) {
        return cachedResponses.containsKey(view.toString());
    }

    public void setCache(View view, Response response) {
        cachedResponses.put(view.toString(), response);
    }

    public Response getCache(View view) {
        return cachedResponses.get(view.toString());
    }

    public Map<String, Response> getAll() {
        return this.cachedResponses;
    }
}
