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

import lombok.Getter;
import lombok.NonNull;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.util.FileUtils;

import javax.script.Bindings;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

final class ViewScriptEngine extends KvantumScriptEngine {

    @Getter private final Path path;

    ViewScriptEngine(@NonNull final ScriptEngineManager scriptEngineManager,
        @NonNull final Path path) {
        super(scriptEngineManager);
        this.path = path;

        if (this.path.getSubPaths().isEmpty()) {
            try {
                FileUtils.copyResource("scripts/folderStructure.js",
                    path.getPath("folderStructure.js").getJavaPath());
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    Bindings getBindings() {
        return this.getEngine().createBindings();
    }

    boolean evaluate(@NonNull final Path script, @NonNull final Bindings bindings) {
        final String content = script.readFile();
        try {
            this.getEngine().eval(content, bindings);
            return true;
        } catch (final ScriptException e) {
            e.printStackTrace();
        }
        return false;
    }

}
