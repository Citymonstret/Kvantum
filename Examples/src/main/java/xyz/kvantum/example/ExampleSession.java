/*
 *    Copyright (C) 2017 IntellectualSites
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

import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.views.staticviews.StaticViewManager;
import xyz.kvantum.server.api.views.staticviews.ViewMatcher;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
class ExampleSession
{

    private static final KvantumPojoFactory<SessionPojo> pojoFactory = KvantumPojoFactory
            .forClass( SessionPojo.class );

    ExampleSession()
    {
        try
        {
            StaticViewManager.generate( this );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    @ViewMatcher(filter = "session", name = "debugSession", httpMethod = HttpMethod.GET)
    public final void debugSession(final AbstractRequest request, final Response response)
    {
        //
        // A simple visit counter
        //
        final AtomicInteger visits;
        if ( !request.getSession().contains( "visits" ) )
        {
            visits = new AtomicInteger( 0 );
            request.getSession().set( "visits", visits );
        } else
        {
            visits = (AtomicInteger) request.getSession().get( "visits" );
        }

        //
        // Convert the java object to a KvantumPojo instance
        //
        final KvantumPojo<SessionPojo> pojo = pojoFactory.of( new SessionPojo( request.getSession() ) );

        //
        // Update the java object
        //
        pojo.set( "message", "You have visited a total of " + visits.incrementAndGet() + " times!" );

        //
        // Add the object to the model
        //
        request.addModel( "pojo", pojo );

        //
        // Render the object
        //
        response.setContent( "<h1><b>Session: {{pojo.id}}</b></h1><br/>Message: {{pojo.message}}" );
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
