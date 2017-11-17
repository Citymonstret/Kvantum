/*
 *
 *    Copyright (C) 2017 IntellectualSites
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
package com.github.intellectualsites.kvantum.api.template;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.config.Message;
import com.github.intellectualsites.kvantum.api.core.WorkerProcedure;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.util.IgnoreSyntax;
import com.github.intellectualsites.kvantum.api.views.RequestHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class TemplateSyntaxHandler extends WorkerProcedure.StringHandler
{

    private final TemplateHandler templateHandler;

    @Override
    public final String act(RequestHandler requestHandler, AbstractRequest request, String in)
    {
        String out = in;
        if ( !( requestHandler instanceof IgnoreSyntax ) )
        {
            if ( !CoreConfig.Templates.applyTemplates.contains( "ALL" ) && !CoreConfig.Templates.applyTemplates
                    .contains( requestHandler.getName() ) )
            {
                if ( CoreConfig.debug )
                {
                    Message.TEMPLATING_ENGINE_DEBUG_NOT_ENABLED.log( requestHandler.getName() );
                }
            } else
            {
                if ( CoreConfig.debug )
                {
                    Message.TEMPLATING_ENGINE_REACTING.log( templateHandler.getEngineName(), request );
                }
                out = this.handle( requestHandler, request, in );
            }
        }
        return out;
    }

    protected abstract String handle(RequestHandler requestHandler, AbstractRequest request, String in);

}
