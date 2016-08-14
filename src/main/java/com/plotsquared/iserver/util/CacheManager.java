//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.util;

import com.plotsquared.iserver.object.ResponseBody;
import com.plotsquared.iserver.object.cache.CachedResponse;
import com.plotsquared.iserver.views.RequestHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The utility file that
 * handles all runtime caching
 */
public class CacheManager
{

    public final Map<String, String> cachedIncludes = new ConcurrentHashMap<>();
    private final Map<String, CachedResponse> cachedBodies = new ConcurrentHashMap<>();

    /**
     * Get a cached include block
     *
     * @param group Include block (matcher.group())
     * @return string|null
     */
    public String getCachedInclude(final String group)
    {
        Assert.notNull( group );

        return this.cachedIncludes.containsKey( group ) ?
                cachedIncludes.get( group ) : null;
    }

    /**
     * Set a cached include block
     *
     * @param group    matcher.group()
     * @param document Generated document
     */
    public void setCachedInclude(final String group, final String document)
    {
        Assert.notNull( group, document );

        this.cachedIncludes.put( group, document );
    }

    /**
     * Check if there is a ResponseBody cached for the view
     *
     * @param view RequestHandler
     * @return true if there is a ResponseBody cached, else false
     */
    public boolean hasCache(final RequestHandler view)
    {
        Assert.notNull( view );

        return this.cachedBodies.containsKey( view.toString() );
    }

    /**
     * Add a cached ResponseBody
     *
     * @param view         RequestHandler for which the caching will apply
     * @param responseBody ResponseBody (will generate a CachedResponseBody)
     * @see CachedResponse
     */
    public void setCache(final RequestHandler view, final ResponseBody responseBody)
    {
        Assert.notNull( view, responseBody );

        this.cachedBodies.put( view.toString(), new CachedResponse( responseBody ) );
    }

    /**
     * Get the cached reponse for a view
     *
     * @param view RequestHandler
     * @return the cached ResponseBody
     * @see #hasCache(RequestHandler) To check if the view has a cache
     */
    public CachedResponse getCache(final RequestHandler view)
    {
        Assert.notNull( view );

        return this.cachedBodies.get( view.toString() );
    }

    /**
     * Get all cached ResponseBodies
     *
     * @return all cached ResponseBodies
     */
    public Map<String, CachedResponse> getAll()
    {
        return this.cachedBodies;
    }
}
