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
package xyz.kvantum.server.api.util;

import java.util.Optional;
import javax.annotation.Nullable;
import xyz.kvantum.server.api.request.AbstractRequest;

/**
 * Factory class for generating per-request {@link VariableProvider}
 *
 * @param <T> Provider type
 */
public interface ProviderFactory<T extends VariableProvider>
{

	/**
	 * Get a variable provider, may be unique for the given request; this depends entirely on the implementation
	 *
	 * @param r Request
	 * @return May return the request; or null
	 */
	Optional<T> get(AbstractRequest r);

	/**
	 * Get the provider name (used in variable mapping)
	 *
	 * @return unique provider name
	 */
	@Nullable String providerName();

}
