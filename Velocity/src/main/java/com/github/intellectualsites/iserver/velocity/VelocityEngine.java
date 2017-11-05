package com.github.intellectualsites.iserver.velocity;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.template.TemplateHandler;
import org.apache.velocity.app.Velocity;

public class VelocityEngine extends TemplateHandler
{

    private static VelocityEngine instance;

    private VelocityEngine()
    {
        super( CoreConfig.TemplatingEngine.VELOCITY, "VelocityEngine" );
    }

    public static VelocityEngine getInstance()
    {
        if ( instance == null )
        {
            instance = new VelocityEngine();
        }
        return instance;
    }

    @Override
    public void onLoad()
    {
        Velocity.init();
        ServerImplementation.getImplementation().getProcedure().addProcedure( "syntax", new SyntaxHandler( this ) );
    }

}
