package xyz.kvantum.example;

import static xyz.kvantum.server.api.views.Routes.get;

import xyz.kvantum.server.implementation.QuickStart;

public class ExampleQuickStart
{

	public static void main(final String[] args)
	{

		QuickStart.newStandaloneServer();
		get( "home", ( request, response ) -> response.setContent( "Home page!" ) );
	}

}
