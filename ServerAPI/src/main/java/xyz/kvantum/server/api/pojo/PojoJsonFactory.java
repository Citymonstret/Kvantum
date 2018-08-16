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
package xyz.kvantum.server.api.pojo;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;

@SuppressWarnings("WeakerAccess") @RequiredArgsConstructor(access = AccessLevel.PACKAGE) public final class PojoJsonFactory<Pojo>
{

	private final KvantumPojoFactory<Pojo> kvantumPojoFactory;

	JSONObject toJson(final KvantumPojo<Pojo> kvantumPojo)
	{
		return new JSONObject( kvantumPojo.getAll() );
	}

	@SuppressWarnings("unused") JSONObject toJson(final Pojo pojo)
	{
		return this.toJson( kvantumPojoFactory.of( pojo ) );
	}

}
