package xyz.kvantum.server.api.scripts;

import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.views.View;

import javax.script.Bindings;
import java.util.Map;

public class ScriptView extends View
{

    private final Path script;
    private final ViewScriptEngine viewScriptEngine;

    public ScriptView(String filter, Map<String, Object> options)
    {
        super( filter, "script", options, HttpMethod.ALL );
        if ( !options.containsKey( "script" ) )
        {
            throw new IllegalArgumentException( "No script provided for script type..." );
        }
        this.viewScriptEngine = ServerImplementation.getImplementation().getScriptManager()
                .getViewScriptEngine();
        this.script = this.viewScriptEngine.getPath().getPath( options.get( "script" ).toString() );
        if ( !script.exists() )
        {
            Logger.error( "Provided script (%s) does not exist...", options.get( "script" ) );
        }
    }

    @Override
    protected void handle(AbstractRequest request, Response response)
    {
        final Bindings bindings = this.viewScriptEngine.getBindings();
        bindings.put( "Kvantum", ServerImplementation.getImplementation() );
        bindings.put( "request", request );
        bindings.put( "response", response );
        bindings.put( "GET", request.getQuery().getParameters() );
        if ( request.getPostRequest() != null )
        {
            bindings.put( "POST", request.getPostRequest().get() );
        }
        bindings.put( "options", this.options );
        if ( !this.viewScriptEngine.evaluate( this.script, bindings ) )
        {
            response.getHeader().set( Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML );
            response.setContent( "Failed to handle script..." );
        }
    }

}
