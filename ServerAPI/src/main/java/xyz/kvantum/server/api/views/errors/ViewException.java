/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
package xyz.kvantum.server.api.views.errors;

import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.FileUtils;
import xyz.kvantum.server.api.views.View;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ViewException extends View {

    private static String template = "not loaded";

    static {
        initTemplate();
    }

    private final Throwable in;

    public ViewException(final Throwable in) {
        super("", "exception");
        this.in = in;
    }

    private static void initTemplate() {
        final String resourcePath = "template/exception.html";
        final Path folder =
            ServerImplementation.getImplementation().getFileSystem().getPath("templates");
        if (!folder.exists()) {
            if (!folder.create()) {
                Message.COULD_NOT_CREATE_FOLDER.log(folder);
                return;
            }
        }
        final Path path = folder.getPath("exception.html");
        if (!path.exists()) {
            if (!path.create()) {
                Logger.error("could not create file: '{}'", path);
                return;
            }
            try {
                FileUtils.copyResource(resourcePath, path.getJavaPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        template = path.readFile();
    }

    @Override public Response generate(AbstractRequest request) {
        StringWriter sw = new StringWriter();
        in.printStackTrace(new PrintWriter(sw));
        return new Response().setResponse(
            template.replace("{{path}}", request.getQuery().getResource())
                .replace("{{exception}}", in.toString()).replace("{{cause}}",
                sw.toString().replace(System.getProperty("line.separator"), "<br/>\n"))
                .replace("{{message}}", in.getMessage() != null ? in.getMessage() : ""));
    }
}
