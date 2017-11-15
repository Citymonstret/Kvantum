/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
