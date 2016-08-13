package com.plotsquared.iserver.crush;

import com.plotsquared.iserver.config.ConfigVariableProvider;
import com.plotsquared.iserver.config.Message;
import com.plotsquared.iserver.core.CoreConfig;
import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.crush.syntax.*;
import com.plotsquared.iserver.plugin.Plugin;
import com.plotsquared.iserver.util.Assert;
import com.plotsquared.iserver.util.MetaProvider;
import com.plotsquared.iserver.util.PostProviderFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

public class CrushEngine extends Plugin
{

    private static CrushEngine instance;

    public static CrushEngine getInstance()
    {
        return instance;
    }

    final Collection<Syntax> syntaxCollection = new LinkedHashSet<>();
    final Collection<ProviderFactory> providers = new ArrayList<>();

    @Override
    public void onEnable()
    {
        Message.SYNTAX_STATUS.log( CoreConfig.enableSyntax );

        if ( !CoreConfig.enableSyntax )
        {
            return;
        }

        instance = this;

        this.syntaxCollection.add( new Include() );
        this.syntaxCollection.add( new Comment() );
        this.syntaxCollection.add( new MetaBlock() );
        this.syntaxCollection.add( new IfStatement() );
        this.syntaxCollection.add( new ForEachBlock() );
        this.syntaxCollection.add( new Variable() );

        this.providers.add( Server.getInstance().getSessionManager() );
        this.providers.add( ConfigVariableProvider.getInstance() );
        this.providers.add( new PostProviderFactory() );
        this.providers.add( new MetaProvider() );

        Server.getInstance().getProcedure().addProcedure( "syntax", new SyntaxHandler( ( Server ) Server.getInstance
                (), this ) );
    }

    public void addProviderFactory(final ProviderFactory factory)
    {
        Assert.notNull( factory );

        this.providers.add( factory );
    }

}
