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

    void setCachedInclude(String group, String document);

    boolean hasCache(RequestHandler view);

    void setCache(RequestHandler view, ResponseBody body);

    CachedResponse getCache(RequestHandler view);

}
