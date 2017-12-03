package xyz.kvantum.server.api.scripts;

import lombok.Getter;
import lombok.NonNull;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.util.FileUtils;

import javax.script.Bindings;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

final class ViewScriptEngine extends KvantumScriptEngine
{

    @Getter
    private final Path path;

    ViewScriptEngine(@NonNull final ScriptEngineManager scriptEngineManager, @NonNull final Path path)
    {
        super( scriptEngineManager );
        this.path = path;

        if ( this.path.getSubPaths().isEmpty() )
        {
            try
            {
                FileUtils.copyResource( "scripts/folderStructure.js", path.getPath( "folderStructure.js" )
                        .getJavaPath() );
            } catch ( final Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    Bindings getBindings()
    {
        return this.getEngine().createBindings();
    }

    boolean evaluate(@NonNull final Path script, @NonNull final Bindings bindings)
    {
        final String content = script.readFile();
        try
        {
            this.getEngine().eval( content, bindings );
            return true;
        } catch ( final ScriptException e )
        {
            e.printStackTrace();
        }
        return false;
    }

}
