package com.github.intellectualsites.iserver.api.cache;

import com.github.intellectualsites.iserver.api.account.IAccount;
import com.github.intellectualsites.iserver.api.response.ResponseBody;
import com.github.intellectualsites.iserver.api.views.RequestHandler;

import java.util.Optional;

/**
 * Interface that manages all IntellectualServer caching.
 */
public interface ICacheManager
{

    /**
     * Get the file content for a cached Crush <pre>{{include}}</pre> file
     * @param file File
     * @return null if the file is not stored in the cache, otherwise
     *         the file content
     */
    String getCachedInclude(String file);

    Optional<IAccount> getCachedAccount(int id);

    Optional<Integer> getCachedId(String username);

    void setCachedAccount(IAccount account);

    Optional<String> getCachedFile(String file);

    void setCachedFile(String file, String content);

    /**
     * Set a cached include block
     *
     * @param group    matcher.group()
     * @param document Generated document
     */
    void setCachedInclude(String group, String document);

    /**
     * Check if there is a ResponseBody cached for the view
     *
     * @param view RequestHandler
     * @return true if there is a ResponseBody cached, else false
     */
    boolean hasCache(RequestHandler view);

    /**
     * Add a cached ResponseBody
     *
     * @param view RequestHandler for which the caching will apply
     * @param body ResponseBody (will generate a CachedResponseBody)
     * @see CachedResponse
     */
    void setCache(RequestHandler view, ResponseBody body);

    /**
     * Get the cached reponse for a view
     *
     * @param view RequestHandler
     * @return the cached ResponseBody
     * @see #hasCache(RequestHandler) To check if the view has a cache
     */
    CachedResponse getCache(RequestHandler view);

}
