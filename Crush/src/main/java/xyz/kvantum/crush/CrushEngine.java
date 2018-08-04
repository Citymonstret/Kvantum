/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.crush;

import xyz.kvantum.crush.syntax.Comment;
import xyz.kvantum.crush.syntax.ForEachBlock;
import xyz.kvantum.crush.syntax.IfStatement;
import xyz.kvantum.crush.syntax.Include;
import xyz.kvantum.crush.syntax.Macro;
import xyz.kvantum.crush.syntax.MetaBlock;
import xyz.kvantum.crush.syntax.Syntax;
import xyz.kvantum.crush.syntax.Variable;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.template.TemplateHandler;

import java.util.Collection;
import java.util.LinkedHashSet;

public final class CrushEngine extends TemplateHandler
{

    private static CrushEngine instance;
    final Collection<Syntax> syntaxCollection = new LinkedHashSet<>();

    private CrushEngine()
    {
        super( CoreConfig.TemplatingEngine.CRUSH, "CrushEngine" );
    }

    static CrushEngine getInstance()
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
        this.syntaxCollection.add( new Macro() );

        ServerImplementation.getImplementation().getProcedure().addProcedure( "syntax", new SyntaxHandler( this ) );
    }
}
