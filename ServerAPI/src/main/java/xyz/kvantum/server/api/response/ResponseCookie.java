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
package xyz.kvantum.server.api.response;

import java.util.Date;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.kvantum.server.api.util.AsciiString;
import xyz.kvantum.server.api.util.AsciiStringable;
import xyz.kvantum.server.api.util.TimeUtil;

@EqualsAndHashCode @Getter @Builder public final class ResponseCookie implements AsciiStringable
{

	private static final String ELEMENT_PATH = "; Path=";
	private static final String ELEMENT_EXPIRES = "; Expires=";
	private static final String ELEMENT_SECURE = "; Secure";
	private static final String ELEMENT_HTTP_ONLY = "; HttpOnly";

	@Builder.Default public AsciiString path = AsciiString.of( "/" );
	private AsciiString cookie;
	private AsciiString value;
	@Builder.Default private Date expires = null;
	@Builder.Default private boolean httpOnly = false;
	@Builder.Default private boolean secure = false;

	private String cache;

	@Override public String toString()
	{
		if ( this.cache == null )
		{
			final StringBuilder builder = new StringBuilder( this.cookie ).append( '=' ).append( value ).append( ELEMENT_PATH ).append( path );
			if ( this.expires != null )
			{
				builder.append( ELEMENT_EXPIRES ).append( TimeUtil.getHTTPTimeStamp( this.expires ) );
			}
			if ( this.secure )
			{
				builder.append( ELEMENT_SECURE );
			}
			if ( this.httpOnly )
			{
				builder.append( ELEMENT_HTTP_ONLY );
			}
			this.cache = builder.toString();
		}
		return this.cache;
	}

	@Override public AsciiString toAsciiString()
	{
		return AsciiString.of( this.toString(), false );
	}
}
