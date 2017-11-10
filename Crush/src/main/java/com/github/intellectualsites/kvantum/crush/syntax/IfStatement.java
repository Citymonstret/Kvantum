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
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     Kvantum is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.github.intellectualsites.kvantum.crush.syntax;

import com.github.intellectualsites.kvantum.api.request.Request;
import com.github.intellectualsites.kvantum.api.util.ProviderFactory;
import com.github.intellectualsites.kvantum.api.util.VariableProvider;

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
    public String process(final String in, final Matcher matcher, final Request r, final Map<String, ProviderFactory>
            factories)
    {
        String workingString = in;
        while ( matcher.find() )
        {
            String neg = matcher.group( 2 );
            String namespace = matcher.group( 3 );
            String variable = matcher.group( 4 );
            if ( factories.containsKey( namespace.toLowerCase() ) )
            {
                Optional<VariableProvider> pOptional = factories.get( namespace.toLowerCase() ).get( r );
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
