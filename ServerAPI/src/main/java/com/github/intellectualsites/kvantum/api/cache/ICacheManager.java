/*
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.intellectualsites.kvantum.api.cache;

import com.github.intellectualsites.kvantum.api.account.IAccount;
import com.github.intellectualsites.kvantum.api.response.ResponseBody;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;

import java.util.Optional;

/**
 * Interface that manages all Kvantum caching.
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

    /**
     * Get a cached account based on the account ID, if it
     * is stored in the cache
     *
     * @param id Account id
     * @return Account if it is stored
     * @see #getCachedId(String) to get the account ID from the account username
     */
    Optional<IAccount> getCachedAccount(int id);

    /**
     * Get a cached account ID, if it is stored in the cache
     * @param username Account username
     * @return Account ID if it is stored
     */
    Optional<Integer> getCachedId(String username);

    /**
     * Save an account to the cache
     * @param account Account to save
     */
    void setCachedAccount(IAccount account);

    /**
     * Get a cached file from the cache, if is stored
     * @param file File name
     * @return File, if it stored
     */
    Optional<String> getCachedFile(String file);

    /**
     * Save an account to the cache
     * @param file File name
     * @param content File content
     */
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
