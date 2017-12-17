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
package xyz.kvantum.crush.syntax;

import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.ProviderFactory;
import xyz.kvantum.server.api.util.VariableProvider;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class IfStatement extends Syntax
{

    public IfStatement()
    {
        super( Pattern.compile( "\\{(#if)( !| )([A-Za-z0-9]*).([A-Za-z0-9_\\-@]*)\\}([\\S\\s]*?)\\{(/if)\\}" ) );
    }

    @Override
    public String process(final String in, final Matcher matcher, final AbstractRequest r, final Map<String, ProviderFactory>
            factories)
    {
        String workingString = in;
        while ( matcher.find() )
        {
            String neg = matcher.group( 2 );
            String namespace = matcher.group( 3 );
            String variable = matcher.group( 4 );
            if ( factories.containsKey( namespace.toLowerCase( Locale.ENGLISH ) ) )
            {
                Optional<VariableProvider> pOptional = factories.get( namespace.toLowerCase( Locale.ENGLISH ) ).get( r );
                if ( pOptional.isPresent() )
                {
                    final VariableProvider p = pOptional.get();
                    if ( p.contains( variable ) )
                    {
                        Object o = p.get( variable );
                        boolean b;
                        if ( o instanceof Boolean )
                        {
                            b = (Boolean) o;
                        } else if ( o instanceof String )
                        {
                            b = o.toString().equalsIgnoreCase( "true" );
                        } else
                            b = o instanceof Number && ( (Number) o ).intValue() == 1;
                        if ( neg.contains( "!" ) )
                        {
                            b = !b;
                        }

                        if ( b )
                        {
                            workingString = workingString.replace( matcher.group(), matcher.group( 5 ) );
                        } else
                        {
                            workingString = workingString.replace( matcher.group(), "" );
                        }
                    }
                }
            }
        }
        return workingString;
    }
}
