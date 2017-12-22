/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
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
package xyz.kvantum.crush.syntax;

import xyz.kvantum.server.api.cache.ICacheManager;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class Include extends Syntax
{

    public Include()
    {
        super( Pattern.compile( "\\{\\{include:([/A-Za-z\\.\\-]*)\\}\\}" ) );
    }

    @Override
    public String process(final String in,
                          final Matcher matcher,
                          final AbstractRequest r,
                          final Map<String, ProviderFactory<? extends VariableProvider>> factories)
    {
        String out = in;
        while ( matcher.find() )
        {
            ICacheManager manager = ServerImplementation.getImplementation().getCacheManager();
            String s = manager.getCachedInclude( matcher.group() );
            if ( s != null )
            {
                out = out.replace( matcher.group(), s );
                continue;
            }

            File file = new File( ServerImplementation.getImplementation().getCoreFolder(), matcher.group( 1 ) );
            if ( file.exists() )
            {
                StringBuilder c = new StringBuilder();
                String line;
                try
                {
                    BufferedReader reader = new BufferedReader( new FileReader( file ) );
                    while ( ( line = reader.readLine() ) != null )
                        c.append( line ).append( "\n" );
                    reader.close();
                } catch ( final Exception e )
                {
                    e.printStackTrace();
                }

                ServerImplementation.getImplementation().getCacheManager().setCachedInclude( matcher.group(), file.getName().endsWith
                        ( ".css" ) ?
                        "<style>\n" + c + "<style>" : c.toString() );

                if ( file.getName().endsWith( ".css" ) )
                {
                    out = out.replace( matcher.group(), "<style>\n" + c + "</style>" );
                } else
                {
                    out = out.replace( matcher.group(), c.toString() );
                }
            } else
            {
                ServerImplementation.getImplementation().log( "Couldn't find file for '{}'", matcher.group() );
            }
        }
        return out;
    }
}
