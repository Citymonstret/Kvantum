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
package xyz.kvantum.server.api.views;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import xyz.kvantum.server.api.cache.CacheApplicable;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.FileExtension;

/**
 * Created 2015-04-22 for Kvantum
 *
 * @author Citymonstret
 */
public class JSView extends StaticFileView implements CacheApplicable
{

	public JSView(String filter, Map<String, Object> options)
	{
		super( filter, options, "javascript", Collections.singletonList( FileExtension.JAVASCRIPT ) );
		super.relatedFolderPath = "/assets/js";
		super.setOption( "extension", "js" );
		super.defaultFilePattern = "${file}.js";
	}

	@Override public boolean isApplicable(AbstractRequest r)
	{
		final Optional<Boolean> cacheApplicableBoolean = getOptionSafe( "cacheApplicable" );
		return cacheApplicableBoolean.orElse( true );
	}

}
