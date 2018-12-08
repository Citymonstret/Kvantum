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
import xyz.kvantum.server.api.core.Kvantum;
import xyz.kvantum.server.api.logging.Logger;

import javax.script.ScriptEngineManager;
import java.util.Arrays;

final class ScriptManager {

    private final Path corePath;

    @Getter private final ViewScriptEngine viewScriptEngine;

    ScriptManager(@NonNull final Kvantum server) {
        this.corePath = server.getFileSystem().getPath("scripts");
        if (!corePath.exists() && !corePath.create()) {
            Logger.error("Failed to create kvantum/scripts - Please do it manually!");
        }
        Arrays.asList("views", "filters").forEach(string -> {
            final Path path = this.corePath.getPath(string);
            if (!path.exists() && !path.create()) {
                Logger
                    .error("Failed to create kvantum/scripts/{} - Please do it manually!", string);
            }
        });
        final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        this.viewScriptEngine =
            new ViewScriptEngine(scriptEngineManager, corePath.getPath("views"));
    }

}
