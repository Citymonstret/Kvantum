package com.github.intellectualsites.iserver.velocity;

import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.template.TemplateHandler;
import com.github.intellectualsites.iserver.api.template.TemplateSyntaxHandler;
import com.github.intellectualsites.iserver.api.templates.TemplateManager;
import com.github.intellectualsites.iserver.api.util.ProviderFactory;
import com.github.intellectualsites.iserver.api.util.VariableProvider;
import com.github.intellectualsites.iserver.api.views.RequestHandler;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SyntaxHandler extends TemplateSyntaxHandler
{

    SyntaxHandler(TemplateHandler templateHandler)
    {
        super( templateHandler );
    }

    @Override
    protected String handle(RequestHandler requestHandler, Request request, String in)
    {
        String out = in;
        final Map<String, ProviderFactory<? extends VariableProvider>> factories = new HashMap<>();
        final Map<String, Object> objects = new HashMap<>();

        for ( final ProviderFactory<? extends VariableProvider> factory : TemplateManager.get().getProviders() )
        {
            factories.put( factory.providerName().toLowerCase(), factory );
        }
        final ProviderFactory z = requestHandler.getFactory( request );
        if ( z != null )
        {
            factories.put( z.providerName().toLowerCase(), z );
        }
        factories.put( "request", request );

        for ( final Map.Entry<String, ProviderFactory<? extends VariableProvider>> entry : factories.entrySet() )
        {
            final Map<String, Object> entryObjects = new HashMap<>();
            final Optional<? extends VariableProvider> providerOptional = entry.getValue().get( request );
            if ( !providerOptional.isPresent() )
            {
                continue;
            }
            final VariableProvider provider = providerOptional.get();
            entryObjects.putAll( provider.getAll() );
            objects.put( entry.getKey(), entryObjects );
        }

        final VelocityContext context = new VelocityContext( objects );
        final StringWriter writer = new StringWriter();
        Velocity.evaluate( context, writer, "SyntaxHandler", out );
        out = writer.toString();
        return out;
    }

}
