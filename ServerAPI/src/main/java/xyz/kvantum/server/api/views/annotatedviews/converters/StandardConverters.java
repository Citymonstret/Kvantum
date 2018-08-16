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
package xyz.kvantum.server.api.views.annotatedviews.converters;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.views.annotatedviews.AnnotatedViewManager;

@UtilityClass public class StandardConverters
{

	public static final String HTML = "html";
	public static final String JSON = "json";
	public static final String XML = "xml";

	public static void registerStandardConverters(@NonNull final AnnotatedViewManager staticViewManager)
	{
		new HtmlConverter( staticViewManager );
		new JsonConverter( staticViewManager );
		new XmlConverter( staticViewManager );
	}

}
