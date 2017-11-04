/*
 * IntellectualServer is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.iserver.implementation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.intellectualsites.iserver.api.account.IAccount;
import com.github.intellectualsites.iserver.api.cache.CachedResponse;
import com.github.intellectualsites.iserver.api.cache.ICacheManager;
import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.response.ResponseBody;
import com.github.intellectualsites.iserver.api.util.Assert;
import com.github.intellectualsites.iserver.api.views.RequestHandler;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The utility file that
 * handles all runtime caching
 */
@SuppressWarnings("ALL")
public final class CacheManager implements ICacheManager
{

    private final Cache<String, String> cachedIncludes;
    private final Cache<String, String> cachedFiles;
    private final Cache<String, CachedResponse> cachedBodies;
    private final Cache<Integer, IAccount> cachedAccounts;
    private final Cache<String, Integer> cachedAccountIds;

    public CacheManager()
    {
        cachedIncludes = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedIncludesExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedIncludesMaxItems ).build();
        cachedFiles = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedFilesExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedFilesMaxItems ).build();
        cachedBodies = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedBodiesExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedBodiesMaxItems ).build();
        cachedAccounts = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedAccountsExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedAccountsMaxItems ).build();
        cachedAccountIds = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedAccountIdsExpiry, TimeUnit
                .SECONDS ).maximumSize( CoreConfig.Cache.cachedAccountIdsMaxItems ).build();
    }

    @Override
    public String getCachedInclude(final String group)
    {
        Assert.notNull( group );

        return this.cachedIncludes.getIfPresent( group );
    }

    @Override
    public Optional<IAccount> getCachedAccount(final int id)
    {
        return Optional.ofNullable( cachedAccounts.getIfPresent( id ) );
    }

    @Override
    public Optional<Integer> getCachedId(final String username)
    {
        return Optional.ofNullable( cachedAccountIds.getIfPresent( username ) );
    }

    @Override
    public void setCachedAccount(final IAccount account)
    {
        this.cachedAccounts.put( account.getId(), account );
        this.cachedAccountIds.put( account.getUsername(), account.getId() );
    }

    @Override
    public Optional<String> getCachedFile(final String file)
    {
        Assert.notEmpty( file );

        if ( CoreConfig.debug )
        {
            Message.CACHE_FILE_ACCESS.log( file );
        }

        if ( !CoreConfig.Cache.enabled )
        {
            return Optional.empty();
        }

        return Optional.ofNullable( cachedFiles.getIfPresent( file ) );
    }

    @Override
    public void setCachedFile(final String file, final String content)
    {
        Assert.notEmpty( file );
        Assert.notNull( content );

        cachedFiles.put( file, content );
    }

    @Override
    public void setCachedInclude(final String group, final String document)
    {
        Assert.notNull( group, document );

        this.cachedIncludes.put( group, document );
    }

    @Override
    public boolean hasCache(final RequestHandler view)
    {
        Assert.notNull( view );

        return this.cachedBodies.getIfPresent( view.toString() ) != null;
    }

    @Override
    public void setCache(final RequestHandler view, final ResponseBody responseBody)
    {
        Assert.notNull( view, responseBody );

        this.cachedBodies.put( view.toString(), new CachedResponse( responseBody ) );
    }

    @Override
    public CachedResponse getCache(final RequestHandler view)
    {
        Assert.notNull( view );

        if ( CoreConfig.debug )
        {
            Message.CACHE_REQUEST_ACCESS.log( view );
        }

        return this.cachedBodies.getIfPresent( view.toString() );
    }
}
