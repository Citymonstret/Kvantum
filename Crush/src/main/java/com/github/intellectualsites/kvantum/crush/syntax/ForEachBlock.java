/*
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
package com.github.intellectualsites.kvantum.crush.syntax;

import com.github.intellectualsites.kvantum.api.core.ServerImplementation;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;
import com.github.intellectualsites.kvantum.api.util.VariableProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class ForEachBlock extends Syntax
{

    public ForEachBlock()
    {
        super( Pattern.compile( "\\{#foreach ([A-Za-z0-9]*).([A-Za-z0-9]*) -> ([A-Za-z0-9]*)\\}([A-Za-z0-9<>\"'-_\\/\\\\ }{}\\n\\s]*)\\{\\/foreach\\}" ) );
    }

    public String process(String content, Matcher matcher, AbstractRequest r, Map<String, ProviderFactory> factories)
    {
        while ( matcher.find() )
        {
            String provider = matcher.group( 1 );
            String variable = matcher.group( 2 );
            String variableName = matcher.group( 3 );
            String forContent = matcher.group( 4 );

            try
            {
                if ( factories.containsKey( provider.toLowerCase() ) )
                {
                    Optional<VariableProvider> pOptional = factories.get( provider.toLowerCase() ).get( r );
                    if ( pOptional.isPresent() )
                    {
                        final VariableProvider p = pOptional.get();
                        if ( !variable.equalsIgnoreCase( "ALL" ) && !p.contains( variable ) )
                        {
                            content = content.replace( matcher.group(), "" );
                        } else
                        {
                            Object o = variable.equalsIgnoreCase( "ALL" ) ? p.getAll().values() : p.get( variable );

                            StringBuilder totalContent = new StringBuilder();
                            if ( o instanceof Object[] )
                            {
                                for ( Object oo : (Object[]) o )
                                {
                                    if ( oo == null )
                                    {
                                        continue;
                                    }
                                    totalContent.append( forContent.replace( "{{" + variableName + "}}", oo.toString() ) );
                                }
                            } else if ( o instanceof Collection )
                            {
                                for ( Object oo : (Collection) o )
                                {
                                    if ( oo == null )
                                    {
                                        continue;
                                    }
                                    totalContent.append( forContent.replace( "{{" + variableName + "}}", oo.toString() ) );
                                }
                            }
                            content = content.replace( matcher.group(), totalContent.toString() );
                        }
                    } else
                    {
                        content = content.replace( matcher.group(), "" );
                    }
                } else
                {
                    content = content.replace( matcher.group(), "" );
                }
            } catch ( final Exception e )
            {
                ServerImplementation.getImplementation().log( "Failed to finish the for loop (" + provider + "." + variable + " -> " + variableName + ") -> " + e.getMessage() );
            }
        }
        return content;
    }
}
