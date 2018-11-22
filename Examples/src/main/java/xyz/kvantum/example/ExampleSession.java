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
package xyz.kvantum.example;

import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.views.annotatedviews.ViewMatcher;

@SuppressWarnings("unused") class ExampleSession
{

	private static final KvantumPojoFactory<SessionPojo> pojoFactory = KvantumPojoFactory.forClass( SessionPojo.class );

	ExampleSession()
	{
		ServerImplementation.getImplementation().getRouter().scanAndAdd( this );
	}

	@ViewMatcher(filter = "session", name = "debugSession", httpMethod = HttpMethod.GET) public final void debugSession(
			final AbstractRequest request, final Response response)
	{
		//
		// A simple visit counter
		//
		final Counter counter = request.getSession().getOrCompute( "visits", string -> new Counter() );

		//
		// Convert the java object to a KvantumPojo instance
		//
		final KvantumPojo<SessionPojo> pojo = pojoFactory.of( new SessionPojo( request.getSession() ) );

		//
		// Update the java object
		//
		pojo.set( "message", "You have visited a total of " + counter.increment() + " times!" );

		//
		// Add the object to the model
		//
		request.addModel( "pojo", pojo );

		//
		// Render the object
		//
		response.setResponse( "<h1><b>Session: {{pojo.id}}</b></h1><br/>Message: {{pojo.message}}" );
	}

	private static final class Counter
	{

		private int visits;

		private Counter()
		{
			this.visits = 0;
		}

		private synchronized int increment()
		{
			return ++visits;
		}
	}

	private static final class SessionPojo
	{

		private String id;
		private String message;

		private SessionPojo(final ISession iSession)
		{
			this.id = iSession.get( "id" ).toString();
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getMessage()
		{
			return message;
		}

		public void setMessage(String message)
		{
			this.message = message;
		}
	}

}
