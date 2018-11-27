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

import lombok.Getter;
import lombok.NonNull;

/**
 * Response stream with a single input write
 */
public class SimpleResponseStream extends ResponseStream implements KnownLengthStream
{

	@Getter
	private byte[] bytes;
	private int read = 0;

	public SimpleResponseStream(final byte[] bytes)
	{
		this.bytes = new byte[ bytes.length ];
		System.arraycopy( bytes, 0, this.bytes, 0, bytes.length );
	}

	@Override public byte[] read(int amount)
	{
		int toRead = Math.min( this.getOffer(), amount );
		final byte[] bytes = new byte[ toRead ];
		System.arraycopy( this.bytes, read, bytes, 0, toRead );
		this.read += bytes.length;
		if ( this.bytes.length <= this.read )
		{
			this.finish();
		}
		return bytes;
	}

	@Override public int getOffer()
	{
		return this.bytes.length - read;
	}

	@Override public int getLength()
	{
		return this.getBytes().length;
	}

	@Override public byte[] getAll()
	{
		return this.getBytes();
	}

	@Override public void replaceBytes(@NonNull final byte[] bytes)
	{
		this.bytes = bytes;
		this.read = 0;
	}

}
