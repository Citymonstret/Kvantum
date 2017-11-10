/*
 * Kvantum is a web server, written entirely in the Java language.
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
package com.github.intellectualsites.kvantum.crush;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.template.TemplateHandler;
import com.github.intellectualsites.kvantum.crush.syntax.*;

import java.util.Collection;
import java.util.LinkedHashSet;

public class CrushEngine extends TemplateHandler
{

    private static CrushEngine instance;
    final Collection<Syntax> syntaxCollection = new LinkedHashSet<>();

    private CrushEngine()
    {
        super( CoreConfig.TemplatingEngine.CRUSH, "CrushEngine" );
    }

    public static CrushEngine getInstance()
    {
        if ( instance == null )
        {
            instance = new CrushEngine();
        }
        return instance;
    }

    public void onLoad()
    {
        this.syntaxCollection.add( new Include() );
        this.syntaxCollection.add( new Comment() );
        this.syntaxCollection.add( new MetaBlock() );
        this.syntaxCollection.add( new IfStatement() );
        this.syntaxCollection.add( new ForEachBlock() );
        this.syntaxCollection.add( new Variable() );

        ServerImplementation.getImplementation().getProcedure().addProcedure( "syntax", new SyntaxHandler( this ) );
    }
}
