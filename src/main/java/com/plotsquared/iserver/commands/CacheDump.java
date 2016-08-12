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

package com.plotsquared.iserver.commands;

import com.plotsquared.iserver.core.Server;
import com.plotsquared.iserver.object.cache.CachedResponse;
import com.plotsquared.iserver.util.CacheManager;

import java.util.Map;

public class CacheDump extends Command
{

    @Override
    public void handle(String[] args)
    {
        CacheManager cacheManager = Server.getInstance().getCacheManager();
        StringBuilder output = new StringBuilder( "Currently Cached Responses: " );
        for ( Map.Entry<String, CachedResponse> e : cacheManager.getAll().entrySet() )
        {
            output.append( e.getKey() ).append( " = " ).append( e.getValue().isText() ? "text" : "bytes" ).append( ", " );
        }
        output.append( "\n" ).append( "Cached Includes: " );
        for ( String s : cacheManager.cachedIncludes.keySet() )
        {
            output.append( s ).append( ", " );
        }
        Server.getInstance().log( output.toString() );
    }

}
