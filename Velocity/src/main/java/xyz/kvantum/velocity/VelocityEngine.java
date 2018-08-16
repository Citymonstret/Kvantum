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
package xyz.kvantum.velocity;

import org.apache.velocity.app.Velocity;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.template.TemplateHandler;

public class VelocityEngine extends TemplateHandler
{

	private static VelocityEngine instance;

	private VelocityEngine()
	{
		super( CoreConfig.TemplatingEngine.VELOCITY, "VelocityEngine" );
	}

	public static VelocityEngine getInstance()
	{
		if ( instance == null )
		{
			instance = new VelocityEngine();
		}
		return instance;
	}

	@Override public void onLoad()
	{
		Velocity.init();
		ServerImplementation.getImplementation().getProcedure().addProcedure( "syntax", new SyntaxHandler( this ) );
	}

}
