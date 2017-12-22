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
package xyz.kvantum.crush.syntax.filter;

import xyz.kvantum.crush.syntax.Filter;

import java.util.Arrays;
import java.util.Iterator;

final public class Javascript extends Filter
{

    public Javascript()
    {
        super( "javascript" );
    }

    public Object handle(String objectName, Object o)
    {
        StringBuilder s = new StringBuilder();
        s.append( "var " ).append( objectName ).append( " = " );
        if ( o instanceof Object[] )
        {
            Object[] oo = (Object[]) o;
            s.append( "[\n" );
            Iterator iterator = Arrays.asList( oo ).iterator();
            while ( iterator.hasNext() )
            {
                Object ooo = iterator.next();
                handleObject( s, ooo );
                if ( iterator.hasNext() )
                {
                    s.append( ",\n" );
                }
            }
            s.append( "]" );
        } else
        {
            handleObject( s, o );
        }
        return s.append( ";" ).toString();
    }

    private void handleObject(StringBuilder s, Object o)
    {
        if ( o instanceof Number || o instanceof Boolean )
        {
            s.append( o );
        } else if ( o instanceof Object[] )
        {
            for ( Object oo : (Object[]) o )
            {
                handleObject( s, oo );
            }
        } else
        {
            s.append( "\"" ).append( o.toString() ).append( "\"" );
        }
    }
}
