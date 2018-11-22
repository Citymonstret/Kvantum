/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
package xyz.kvantum.server.implementation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import xyz.kvantum.files.CachedFile;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.cache.CachedResponse;
import xyz.kvantum.server.api.cache.ICacheManager;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.response.ResponseBody;
import xyz.kvantum.server.api.views.RequestHandler;

/**
 * The utility file that handles all runtime caching
 */
@SuppressWarnings("ALL") public final class CacheManager implements ICacheManager
{

	private final Cache<String, String> cachedIncludes;
	private final Cache<String, CachedFile> cachedFiles;
	private final Cache<Integer, IAccount> cachedAccounts;
	private final Cache<String, Integer> cachedAccountIds;
	private final Cache<String, CachedResponse> cachedBodies;

	public CacheManager()
	{
		cachedIncludes = Caffeine.newBuilder()
				.expireAfterWrite( CoreConfig.Cache.cachedIncludesExpiry, TimeUnit.SECONDS )
				.maximumSize( CoreConfig.Cache.cachedIncludesMaxItems ).build();
		cachedFiles = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedFilesExpiry, TimeUnit.SECONDS )
				.maximumSize( CoreConfig.Cache.cachedFilesMaxItems ).build();
		cachedAccounts = Caffeine.newBuilder()
				.expireAfterWrite( CoreConfig.Cache.cachedAccountsExpiry, TimeUnit.SECONDS )
				.maximumSize( CoreConfig.Cache.cachedAccountsMaxItems ).build();
		cachedBodies = Caffeine.newBuilder().expireAfterWrite( CoreConfig.Cache.cachedBodiesExpiry, TimeUnit.SECONDS )
				.maximumSize( CoreConfig.Cache.cachedBodiesMaxItems ).build();
		cachedAccountIds = Caffeine.newBuilder()
				.expireAfterWrite( CoreConfig.Cache.cachedAccountIdsExpiry, TimeUnit.SECONDS )
				.maximumSize( CoreConfig.Cache.cachedAccountIdsMaxItems ).build();
	}

	@Override public String getCachedInclude(@NonNull final String group)
	{
		return this.cachedIncludes.getIfPresent( group );
	}

	@Override public Optional<IAccount> getCachedAccount(final int id)
	{
		return Optional.ofNullable( cachedAccounts.getIfPresent( id ) );
	}

	@Override public Optional<Integer> getCachedId(@NonNull final String username)
	{
		return Optional.ofNullable( cachedAccountIds.getIfPresent( username ) );
	}

	@Override public void setCachedAccount(@NonNull final IAccount account)
	{
		this.cachedAccounts.put( account.getId(), account );
		this.cachedAccountIds.put( account.getUsername(), account.getId() );
	}

	@Override public void deleteAccount(@NonNull final IAccount account)
	{
		this.cachedAccounts.invalidate( account.getId() );
		this.cachedAccountIds.invalidate( account.getUsername() );
	}

	@Override public Optional<CachedFile> getCachedFile(@NonNull final Path file)
	{
		return Optional.ofNullable( cachedFiles.getIfPresent( file.toString() ) );
	}

	@Override public void setCachedFile(@NonNull final Path file, @NonNull final CachedFile content)
	{
		cachedFiles.put( file.toString(), content );
	}

	@Override public void setCachedInclude(@NonNull final String group, @NonNull final String document)
	{
		this.cachedIncludes.put( group, document );
	}

	@Override public void removeFileCache(@NonNull final Path path)
	{
		this.cachedFiles.invalidate( path.toString() );
	}

	@Override public boolean hasCache(@NonNull final RequestHandler view)
	{
		return this.cachedBodies.getIfPresent( view.toString() ) != null;
	}
	@Override public void setCache(@NonNull final RequestHandler view, @NonNull final ResponseBody responseBody)
	{
		this.cachedBodies.put( view.toString(), new CachedResponse( responseBody ) );
	}
	@Override public CachedResponse getCache(@NonNull final RequestHandler view)
	{
		return this.cachedBodies.getIfPresent( view.toString() );
	}

}
