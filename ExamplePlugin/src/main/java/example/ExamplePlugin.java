package example;

import com.plotsquared.iserver.api.core.ServerImplementation;
import com.plotsquared.iserver.api.plugin.Plugin;
import com.plotsquared.iserver.api.request.Request;
import com.plotsquared.iserver.api.response.Response;
import com.plotsquared.iserver.api.views.staticviews.StaticViewManager;
import com.plotsquared.iserver.api.views.staticviews.ViewMatcher;

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
