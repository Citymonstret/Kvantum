/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.request;

import java.util.Map;
import java.util.Optional;
import lombok.NoArgsConstructor;
import xyz.kvantum.server.api.request.post.PostRequest;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.MapUtil;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;

/**
 * Created 2015-04-25 for Kvantum
 *
 * @author Citymonstret
 */
@NoArgsConstructor public final class PostProviderFactory
		implements ProviderFactory<PostProviderFactory>, VariableProvider
{

	private PostRequest p;

	private PostProviderFactory(final PostRequest p)
	{
		this.p = p;
	}

	@Override public Optional<PostProviderFactory> get(final AbstractRequest r)
	{
		Assert.notNull( r );

		if ( r.getPostRequest() == null )
		{
			return Optional.empty();
		}
		return Optional.of( new PostProviderFactory( r.getPostRequest() ) );
	}

	@Override public String providerName()
	{
		return "post";
	}

	@Override public boolean contains(final String variable)
	{
		Assert.notNull( variable );

		return p.contains( variable );
	}

	@Override public Object get(final String variable)
	{
		Assert.notNull( variable );

		return p.get( variable );
	}

	@Override public Map<String, Object> getAll()
	{
		return MapUtil.convertMap( this.p.get(), (s) -> s );
	}
}
