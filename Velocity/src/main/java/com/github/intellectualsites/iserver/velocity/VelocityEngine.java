package com.github.intellectualsites.iserver.velocity;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import org.apache.velocity.app.Velocity;

public class VelocityEngine
{

    private static VelocityEngine instance;


    private VelocityEngine()
    {
    }

    public static VelocityEngine getInstance()
    {
        if ( instance == null )
        {
            instance = new VelocityEngine();
        }
        return instance;
    }

    public void load()
    {
        Message.TEMPLATING_ENGINE_STATUS.log( "VelocityEngine",
                CoreConfig.Templates.status( CoreConfig.TemplatingEngine.VELOCITY ) );


        if ( !CoreConfig.Templates.status( CoreConfig.TemplatingEngine.VELOCITY ) )
        {
            return;
        }

        Velocity.init();
        ServerImplementation.getImplementation().getProcedure().addProcedure( "syntax", new SyntaxHandler() );
    }

}
