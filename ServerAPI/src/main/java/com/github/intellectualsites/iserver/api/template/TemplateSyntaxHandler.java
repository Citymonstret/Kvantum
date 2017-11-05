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
