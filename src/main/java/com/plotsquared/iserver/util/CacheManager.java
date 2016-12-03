/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
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
package com.plotsquared.iserver.util;

import com.plotsquared.iserver.account.Account;
import com.plotsquared.iserver.core.CoreConfig;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.ResponseBody;
import com.plotsquared.iserver.object.cache.CachedResponse;
import com.plotsquared.iserver.views.RequestHandler;
import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The utility file that
 * handles all runtime caching
 */
@SuppressWarnings("ALL")
public class CacheManager
{

    private Cache<String, String> cachedIncludes;
    private Cache<String, String> cachedFiles;
    private Cache<String, CachedResponse> cachedBodies;
    private Cache<Integer, Account> cachedAccounts;
    private Cache<String, Integer> cachedAccountIds;

    public CacheManager()
    {
        org.ehcache.CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache( "cachedIncludes", CacheConfigurationBuilder.newCacheConfigurationBuilder( String.class,
                        String.class, ResourcePoolsBuilder.newResourcePoolsBuilder().heap( CoreConfig.Cache.cachedIncludesHeapMB,
                                MemoryUnit.MB ) ).withExpiry( Expirations.timeToLiveExpiration( Duration.of(
                        CoreConfig.Cache.cachedIncludesExpiry, TimeUnit.SECONDS ) )
                ) )
                .withCache( "cachedBodies", CacheConfigurationBuilder.newCacheConfigurationBuilder( String.class,
                        CachedResponse.class, ResourcePoolsBuilder.newResourcePoolsBuilder().heap( CoreConfig.Cache
                                .cachedBodiesHeapMB, MemoryUnit.MB ) ).withExpiry( Expirations
                        .timeToLiveExpiration( Duration.of( CoreConfig.Cache.cachedBodiesExpiry, TimeUnit.SECONDS ) )
                ) )
                .withCache( "cachedFiles", CacheConfigurationBuilder.newCacheConfigurationBuilder( String.class,
                        String.class, ResourcePoolsBuilder.newResourcePoolsBuilder().heap( CoreConfig.Cache
                                .cachedFilesHeapMB, MemoryUnit.MB ) ).withExpiry( Expirations
                        .timeToLiveExpiration( Duration.of( CoreConfig.Cache.cachedFilesExpiry, TimeUnit.SECONDS ) )
                ) )
                .withCache( "cachedAccounts", CacheConfigurationBuilder.newCacheConfigurationBuilder( Integer.class,
                        Account.class, ResourcePoolsBuilder.newResourcePoolsBuilder().heap( CoreConfig.Cache
                                .cachedAccountsHeapMB, MemoryUnit.MB ) ).withExpiry( Expirations.timeToLiveExpiration
                        ( Duration.of( CoreConfig.Cache.cachedAccountsExpiry, TimeUnit.SECONDS ) ) ) )
                .withCache( "cachedAccountIds", CacheConfigurationBuilder.newCacheConfigurationBuilder( String.class,
                        Integer.class, ResourcePoolsBuilder.newResourcePoolsBuilder().heap( CoreConfig.Cache
                                .cachedAccountIdsHeapMB, MemoryUnit.MB ) ).withExpiry( Expirations.timeToLiveExpiration
                        ( Duration.of( CoreConfig.Cache.cachedAccountIdsExpiry, TimeUnit.SECONDS ) ) ) )
                .build( true );
        this.cachedIncludes = cacheManager.getCache( "cachedIncludes", String.class, String.class );
        this.cachedFiles = cacheManager.getCache( "cachedFiles", String.class, String.class );
        this.cachedBodies = cacheManager.getCache( "cachedBodies", String.class, CachedResponse.class );
        this.cachedAccounts = cacheManager.getCache( "cachedAccounts", Integer.class, Account.class );
        this.cachedAccountIds = cacheManager.getCache( "cachedAccountIds", String.class, Integer.class );
    }

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

    public Optional<Account> getCachedAccount(final int id)
    {
        return Optional.ofNullable( cachedAccounts.get( id ) );
    }

    public Optional<Integer> getCachedId(final String username)
    {
        return Optional.ofNullable( cachedAccountIds.get( username ) );
    }

    public void setCachedAccount(final Account account)
    {
        this.cachedAccounts.put( account.getId(), account );
        this.cachedAccountIds.put( account.getUsername(), account.getId() );
    }

    public Optional<String> getCachedFile(final String file)
    {
        Assert.notEmpty( file );

        if ( CoreConfig.debug )
        {
            Server.getInstance().log( "Accessing cached file: " + file );
        }

        if ( !CoreConfig.Cache.enabled )
        {
            return Optional.empty();
        }

        if ( cachedFiles.containsKey( file ) )
        {
            return Optional.of( cachedFiles.get( file ) );
        }
        return Optional.empty();
    }

    public void setCachedFile(final String file, final String content)
    {
        Assert.notEmpty( file );
        Assert.notNull( content );

        cachedFiles.put( file, content );
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

        if ( CoreConfig.debug )
        {
            Server.getInstance().log( "Accessing cached body: " + view );
        }

        return this.cachedBodies.get( view.toString() );
    }
}
