/*
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.template;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.core.WorkerProcedure;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.util.IgnoreSyntax;
import com.github.intellectualsites.iserver.api.views.RequestHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class TemplateSyntaxHandler extends WorkerProcedure.StringHandler
{

    private final TemplateHandler templateHandler;

    @Override
    public final String act(RequestHandler requestHandler, Request request, String in)
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

    protected abstract String handle(RequestHandler requestHandler, Request request, String in);

}
