package example;

import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.plugin.Plugin;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.response.Response;
import com.github.intellectualsites.iserver.api.views.staticviews.StaticViewManager;
import com.github.intellectualsites.iserver.api.views.staticviews.ViewMatcher;

public class ExamplePlugin extends Plugin
{

    @Override
    protected void onEnable()
    {
        try
        {
            StaticViewManager.generate( ExamplePlugin.this );
        } catch ( final Exception e )
        {
            e.printStackTrace();
        }
        ServerImplementation.getImplementation().createSimpleRequestHandler( "plugin", ( (request, response) ->
                response.setContent( "The Plugin Works, Mate!" ) ) );
    }

    @ViewMatcher(filter = "test", cache = false, name = "testPlugin")
    public void testFoodie(final Request request, final Response response)
    {
        response.setContent( "This Plugin Loads, Mate!" );
    }
}
