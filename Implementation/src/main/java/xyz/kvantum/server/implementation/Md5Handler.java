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
package xyz.kvantum.server.implementation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.util.Assert;

final class Md5Handler
{

	private final Base64.Encoder encoder;
	private final MessageDigest digest;

	Md5Handler()
	{
		this.encoder = Base64.getMimeEncoder();
		MessageDigest temporary = null;
		try
		{
			temporary = MessageDigest.getInstance( "MD5" );
		} catch ( final NoSuchAlgorithmException e )
		{
			Message.MD5_DIGEST_NOT_FOUND.log( e.getMessage() );
		}
		digest = temporary;
	}

	/**
	 * MD5-ify the input
	 *
	 * @param input Input text to be digested
	 * @return md5-ified digested text
	 */
	String generateChecksum(final byte[] input)
	{
		Assert.notNull( input );

		// Make sure that the buffer is clean
		digest.reset();
		// Update the digest with the current input
		digest.update( input );
		// Now encode it, yay
		return new String( encoder.encode( digest.digest() ) );
	}

}
