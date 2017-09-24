/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.crush;

import com.plotsquared.iserver.api.config.ConfigVariableProvider;
import com.plotsquared.iserver.api.config.CoreConfig;
import com.plotsquared.iserver.api.config.Message;
import com.plotsquared.iserver.api.core.ServerImplementation;
import com.plotsquared.iserver.api.request.PostProviderFactory;
import com.plotsquared.iserver.api.util.Assert;
import com.plotsquared.iserver.api.util.MetaProvider;
import com.plotsquared.iserver.api.util.ProviderFactory;
import com.plotsquared.iserver.crush.syntax.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

public class CrushEngine
{

    private static CrushEngine instance;
    final Collection<Syntax> syntaxCollection = new LinkedHashSet<>();
    final Collection<ProviderFactory> providers = new ArrayList<>();

    private CrushEngine()
    {
    }

    public static CrushEngine getInstance()
    {
        if ( instance == null )
        {
            instance = new CrushEngine();
        }
        return instance;
    }

    public void load()
    {
        Message.TEMPLATING_ENGINE_STATUS.log( "CrushEngine", CoreConfig.Crush.enable );

        if ( !CoreConfig.Crush.enable )
        {
            return;
        }

        this.syntaxCollection.add( new Include() );
        this.syntaxCollection.add( new Comment() );
        this.syntaxCollection.add( new MetaBlock() );
        this.syntaxCollection.add( new IfStatement() );
        this.syntaxCollection.add( new ForEachBlock() );
        this.syntaxCollection.add( new Variable() );

        this.providers.add( ServerImplementation.getImplementation().getSessionManager() );
        this.providers.add( ConfigVariableProvider.getInstance() );
        this.providers.add( new PostProviderFactory() );
        this.providers.add( new MetaProvider() );

        ServerImplementation.getImplementation().getProcedure().addProcedure( "syntax", new SyntaxHandler( ServerImplementation.getImplementation
                (), this ) );
    }

    public void addProviderFactory(final ProviderFactory factory)
    {
        Assert.notNull( factory );

        this.providers.add( factory );
    }

}
