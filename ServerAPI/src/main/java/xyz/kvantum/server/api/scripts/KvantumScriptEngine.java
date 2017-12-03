package xyz.kvantum.server.api.scripts;

import lombok.Getter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import static lombok.AccessLevel.PROTECTED;

abstract class KvantumScriptEngine
{

    @Getter(PROTECTED)
    private final ScriptEngine engine;

    KvantumScriptEngine(final ScriptEngineManager scriptEngineManager)
    {
        this.engine = scriptEngineManager.getEngineByName( "nashorn" );
    }

}
