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

import org.json.simple.JSONObject;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.MapBuilder;
import xyz.kvantum.server.api.views.View;
import xyz.kvantum.server.api.views.staticviews.StaticViewManager;
import xyz.kvantum.server.api.views.staticviews.ViewMatcher;
import xyz.kvantum.server.api.views.staticviews.converters.StandardConverters;

/**
 * Different examples of how to construct the well known "Hello World"
 * example in Kvantum
 */
@SuppressWarnings("unused")
public class HelloWorld
{

    private static final KvantumPojoFactory<HelloDO> factory = KvantumPojoFactory.forClass( HelloDO.class );

    HelloWorld()
    {
        try
        {
            //
            // Scan the instance for @Annotation-based views
            //
            StaticViewManager.generate( this );
        } catch ( Exception e )
        {
            e.printStackTrace();
        }
        //
        // Load the function-based view constructor
        //
        this.helloWorld6();
        //
        // OOP-based view
        //
        ServerImplementation.getImplementation().getRouter().add( new HelloHandler() );
    }

    @ViewMatcher(filter = "hello1")
    public void helloWorld1(final AbstractRequest request, final Response response)
    {
        response.setContent( "<h1>Hello World!</h1>" );
    }

    @ViewMatcher(filter = "hello2")
    public Response helloWorld2(final AbstractRequest request)
    {
        final Response response = new Response();
        response.setContent( "<b>Hello World!</b>" );
        return response;
    }

    @ViewMatcher(filter = "hello3", outputType = StandardConverters.HTML)
    public String helloWorld3(final AbstractRequest request)
    {
        return "Hello World!";
    }

    @ViewMatcher(filter = "hello4", outputType = StandardConverters.JSON)
    public JSONObject helloWorld4(final AbstractRequest request)
    {
        return new JSONObject( MapBuilder.<String, Object>newHashMap()
                .put( "Hello", "World" ).get() );
    }

    @ViewMatcher(filter = "hello5")
    public void helloWorld5(final AbstractRequest request, final Response response)
    {
        final KvantumPojo<HelloDO> hello = factory.of( new HelloDO() );
        hello.set( "hello", "world" );
        request.addModel( "pojo", hello );
        response.setContent( "Hello {{pojo.hello}}!" );
    }

    private void helloWorld6()
    {
        ServerImplementation.getImplementation().createSimpleRequestHandler( "hello6", ( (request, response) ->
                response.setContent( "Hello World :)" ) ) );
    }

    public static final class HelloHandler extends View
    {

        HelloHandler()
        {
            super( "hello7", "hello7handler" );
        }

        @Override
        protected void handle(AbstractRequest request, Response response)
        {
            response.setContent( "Hello World!!!!" );
        }
    }

    public static final class HelloDO
    {

        String hello;

        HelloDO()
        {
        }

        public HelloDO(final String string)
        {
            this.hello = string;
        }

        public void setHello(final String string)
        {
            this.hello = string;
        }

        public String getHello()
        {
            return this.hello;
        }
    }

}
