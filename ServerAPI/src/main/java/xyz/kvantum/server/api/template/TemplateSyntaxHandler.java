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
package xyz.kvantum.server.api.template;

import lombok.RequiredArgsConstructor;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.config.Message;
import xyz.kvantum.server.api.core.WorkerProcedure;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.IgnoreSyntax;
import xyz.kvantum.server.api.views.RequestHandler;

/**
 * {@inheritDoc}
 */
@RequiredArgsConstructor public abstract class TemplateSyntaxHandler
    extends WorkerProcedure.StringHandler {

    private final TemplateHandler templateHandler;

    @Override
    public final String act(RequestHandler requestHandler, AbstractRequest request, String in) {
        String out = in;
        if (!(requestHandler instanceof IgnoreSyntax)) {
            if (!CoreConfig.Templates.applyTemplates.contains("ALL")
                && !CoreConfig.Templates.applyTemplates.contains(requestHandler.getName())) {
                if (CoreConfig.debug) {
                    Message.TEMPLATING_ENGINE_DEBUG_NOT_ENABLED.log(requestHandler.getName());
                }
            } else {
                if (CoreConfig.debug) {
                    Message.TEMPLATING_ENGINE_REACTING
                        .log(templateHandler.getEngineName(), request);
                }
                out = this.handle(requestHandler, request, in);
            }
        }
        return out;
    }

    protected abstract String handle(RequestHandler requestHandler, AbstractRequest request,
        String in);

}
