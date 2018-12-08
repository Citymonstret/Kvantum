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

public class ScriptView extends View {

    private static ScriptManager scriptManager;
    private final Path script;
    private final ViewScriptEngine viewScriptEngine;

    public ScriptView(final String filter, final Map<String, Object> options) {
        super(filter, "script", options, HttpMethod.ALL);
        if (!options.containsKey("script")) {
            throw new IllegalArgumentException("No script provided for script type...");
        }
        this.viewScriptEngine = getScriptManager().getViewScriptEngine();
        this.script = this.viewScriptEngine.getPath().getPath(options.get("script").toString());
        if (!script.exists()) {
            Logger.error("Provided script ({}) does not exist...", options.get("script"));
        }
    }

    private static ScriptManager getScriptManager() {
        if (scriptManager == null) {
            scriptManager = new ScriptManager(ServerImplementation.getImplementation());
        }
        return scriptManager;
    }

    @Override protected void handle(final AbstractRequest request, final Response response) {
        final Bindings bindings = this.viewScriptEngine.getBindings();
        bindings.put("Kvantum", ServerImplementation.getImplementation());
        bindings.put("request", request);
        bindings.put("response", response);
        bindings.put("GET", request.getQuery().getParameters());
        if (request.getPostRequest() != null) {
            bindings.put("POST", request.getPostRequest().get());
        }
        bindings.put("options", this.options);
        if (!this.viewScriptEngine.evaluate(this.script, bindings)) {
            response.getHeader().set(Header.HEADER_CONTENT_TYPE, Header.CONTENT_TYPE_HTML);
            response.setResponse("Failed to handle script...");
        }
    }

}
