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
package com.github.intellectualsites.kvantum.implementation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.intellectualsites.kvantum.api.account.IAccount;
import com.github.intellectualsites.kvantum.api.cache.CachedResponse;
import com.github.intellectualsites.kvantum.api.cache.ICacheManager;
import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.response.ResponseBody;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;

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
