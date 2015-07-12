package com.intellectualsites.web.util;

import com.intellectualsites.web.object.cache.CachedResponse;
import com.intellectualsites.web.object.Response;
import com.intellectualsites.web.views.View;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The utility file that
 * handles all runtime caching
 */
public class CacheManager {

    public final Map<String, String> cachedIncludes;
    private final Map<String, CachedResponse> cachedResponses;

    /**
     * Constructor
     */
    public CacheManager() {
        this.cachedResponses = new ConcurrentHashMap<>();
        this.cachedIncludes = new ConcurrentHashMap<>();
    }

    /**
     * Get a cached include block
     * @param group Include block (matcher.group())
     * @return string|null
     */
    public String getCachedInclude(final String group) {
        return cachedIncludes.containsKey(group) ? cachedIncludes.get(group) : null;
    }

    /**
     * Set a cached include block
     * @param group matcher.group()
     * @param document Generated document
     */
    public void setCachedInclude(final String group, final String document) {
        this.cachedIncludes.put(group, document);
    }

    /**
     * Check if there is a response cached for the view
     * @param view View
     * @return true if there is a response cached, else false
     */
    public boolean hasCache(View view) {
        return cachedResponses.containsKey(view.toString());
    }

    /**
     * Add a cached response
     * @param view View for which the caching will apply
     * @param response Response (will generate a CachedResponse)
     * @see CachedResponse
     */
    public void setCache(View view, Response response) {
        cachedResponses.put(view.toString(), new CachedResponse(response));
    }

    /**
     * Get the cached reponse for a view
     * @param view View
     * @return the cached response
     * @see #hasCache(View) To check if the view has a cache
     */
    public CachedResponse getCache(View view) {
        return cachedResponses.get(view.toString());
    }

    /**
     * Get all cached responses
     * @return all cached responses
     */
    public Map<String, CachedResponse> getAll() {
        return this.cachedResponses;
    }
}
