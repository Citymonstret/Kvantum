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
package xyz.kvantum.crush.syntax;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;

final public class MetaBlock extends Syntax
{

	private final MetaBlockStatement statement;

	public MetaBlock()
	{
		super( Pattern.compile( "\\{\\{:([\\S\\s]*?):\\}\\}" ) );
		this.statement = new MetaBlockStatement();
	}

	@Override public String process(String in, Matcher matcher, AbstractRequest r,
			Map<String, ProviderFactory<? extends VariableProvider>> factories)
	{
		while ( matcher.find() )
		{
			statement.handle( in, r, factories );
			in = in.replace( matcher.group(), "" );
		}
		return in;
	}
}
