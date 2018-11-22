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
package xyz.kvantum.server.api.views.requesthandler;

import java.util.ArrayDeque;
import java.util.Queue;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.Assert;

public final class MiddlewareQueue
{

	private final Queue<Middleware> queue = new ArrayDeque<>();

	private boolean finished = false;

	public void add(final Middleware middleware)
	{
		this.queue.add( middleware );
	}

	public void handle(final AbstractRequest request)
	{
		Assert.isValid( request );

		final Middleware next = this.queue.poll();
		if ( next != null )
		{
			next.handle( request, this );
		} else
		{
			finished = true;
		}
	}

	public final boolean finished()
	{
		return finished;
	}

}
