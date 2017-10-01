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

package com.github.intellectualsites.iserver.crush.syntax;

import com.github.intellectualsites.iserver.api.cache.CacheManager;
import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;
import com.github.intellectualsites.iserver.api.request.Request;
import com.github.intellectualsites.iserver.api.util.ProviderFactory;

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
    public String process(String in, Matcher matcher, Request r, Map<String, ProviderFactory> factories)
    {
        while ( matcher.find() )
        {
            boolean setCache = false;
            if ( CoreConfig.Cache.enabled )
            {
                CacheManager manager = ServerImplementation.getImplementation().getCacheManager();
                String s = manager.getCachedInclude( matcher.group() );
                if ( s != null )
                {
                    in = in.replace( matcher.group(), s );
                    continue;
                } else
                {
                    setCache = true;
                }
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

                if ( setCache )
                {
                    ServerImplementation.getImplementation().getCacheManager().setCachedInclude( matcher.group(), file.getName().endsWith
                            ( ".css" ) ?
                            "<style>\n" + c + "<style>" : c.toString() );
                }

                if ( file.getName().endsWith( ".css" ) )
                {
                    in = in.replace( matcher.group(), "<style>\n" + c + "</style>" );
                } else
                {
                    in = in.replace( matcher.group(), c.toString() );
                }
            } else
            {
                ServerImplementation.getImplementation().log( "Couldn't find file for '%s'", matcher.group() );
            }
        }
        return in;
    }
}
