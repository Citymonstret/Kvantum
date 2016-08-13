//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
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

package com.plotsquared.iserver.crush.syntax;

import com.plotsquared.iserver.object.Request;
import com.plotsquared.iserver.crush.syntax.filter.Javascript;
import com.plotsquared.iserver.crush.syntax.filter.List;
import com.plotsquared.iserver.crush.syntax.filter.Lowercase;
import com.plotsquared.iserver.crush.syntax.filter.Uppercase;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class Variable extends Syntax
{

    private final Map<String, Filter> filters;

    public Variable()
    {
        super( Pattern.compile( "\\{\\{([a-zA-Z0-9]*)\\.([@A-Za-z0-9_\\-]*)( [|]{2} [A-Z]*)?\\}\\}" ) );
        filters = new HashMap<>();
        Set<Filter> preFilters = new LinkedHashSet<>();
        preFilters.add( new Uppercase() );
        preFilters.add( new Lowercase() );
        preFilters.add( new List() );
        preFilters.add( new Javascript() );
        for ( final Filter filter : preFilters )
        {
            filters.put( filter.toString(), filter );
        }
    }

    @Override
    public String process(String content, Matcher matcher, Request r, Map<String, ProviderFactory> factories)
    {
        while ( matcher.find() )
        {
            String provider = matcher.group( 1 );
            String variable = matcher.group( 2 );

            String filter = "";
            if ( matcher.group().contains( " || " ) )
            {
                filter = matcher.group().split( " \\|\\| " )[ 1 ].replace( "}}", "" );
            }
            if ( factories.containsKey( provider.toLowerCase() ) )
            {
                try
                {
                    VariableProvider p = factories.get( provider.toLowerCase() ).get( r );
                    if ( p != null )
                    {
                        if ( p.contains( variable ) )
                        {
                            Object o = p.get( variable );
                            if ( !filter.equals( "" ) )
                            {
                                o = filters.get( filter.toUpperCase() ).handle( variable, o );
                            }
                            content = content.replace( matcher.group(), o.toString() );
                        }
                    } else
                    {
                        content = content.replace( matcher.group(), "" );
                    }
                } catch ( final Throwable e )
                {
                    e.printStackTrace();
                    content = content.replace( matcher.group(), "" );
                }
            } else
            {
                content = content.replace( matcher.group(), "" );
            }
        }
        return content;
    }
}
