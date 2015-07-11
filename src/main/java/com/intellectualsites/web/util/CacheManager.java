package com.intellectualsites.web.util;

import com.intellectualsites.web.object.cache.CachedResponse;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.View;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    public final Map<String, String> cachedIncludes;
    private final Map<String, CachedResponse> cachedResponses;

    public CacheManager() {
        this.cachedResponses = new ConcurrentHashMap<>();
        this.cachedIncludes = new ConcurrentHashMap<>();
    }

    public String getCachedInclude(final String group) {
        return cachedIncludes.containsKey(group) ? cachedIncludes.get(group) : null;
    }

    public void setCachedInclude(final String group, final String document) {
        this.cachedIncludes.put(group, document);
    }

    public boolean hasCache(View view) {
        return cachedResponses.containsKey(view.toString());
    }

    public void setCache(View view, Response response) {
        cachedResponses.put(view.toString(), new CachedResponse(response));
    }

    public CachedResponse getCache(View view) {
        return cachedResponses.get(view.toString());
    }

    public Map<String, CachedResponse> getAll() {
        return this.cachedResponses;
    }
}
