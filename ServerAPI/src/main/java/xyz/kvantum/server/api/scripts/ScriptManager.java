package xyz.kvantum.server.api.scripts;

import lombok.Getter;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.logging.Logger;

import javax.script.ScriptEngineManager;
import java.util.Arrays;

public class ScriptManager
{

    private final Path corePath;

    @Getter
    private final ViewScriptEngine viewScriptEngine;

    public ScriptManager(final Kvantum server)
    {
        this.corePath = server.getFileSystem().getPath( "scripts" );
        if ( !corePath.exists() && !corePath.create() )
        {
            Logger.error( "Failed to create kvantum/scripts - Please do it manually!" );
        }
        Arrays.asList( "views", "filters" ).forEach( string ->
        {
            final Path path = this.corePath.getPath( string );
            if ( !path.exists() && !path.create() )
            {
                Logger.error( "Failed to create kvantum/scripts/%s - Please do it manually!", string );
            }
        } );
        final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        this.viewScriptEngine = new ViewScriptEngine( scriptEngineManager, corePath.getPath( "views" ) );
    }

}
