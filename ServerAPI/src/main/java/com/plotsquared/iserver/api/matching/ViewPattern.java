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
package com.plotsquared.iserver.api.matching;

import com.plotsquared.iserver.api.core.ServerImplementation;
import com.plotsquared.iserver.api.util.Assert;

import java.util.*;

/**
 * <p>
 *  ViewPattern will be referred to as filter
 * </p>
 * <p>
 *  Filters are used to determine which view gets to serve the incoming requests.
 *  Each filter is made up of different parts, and there are four types of parts:
 *  <ul>
 *      <li>Separator - / - Used like a path separator</li>
 *      <li>Static - Example: user - A static string</li>
 *      <li>Required Variable - Example: &lt;username&gt;</li>
 *      <li>Optional Variable - Example: [page]</li>
 *  </ul>
 * </p>
 * <p>
 *     <b>Examples</b>
 * </p>
 * <p>
 *     <b>user/&lt;username&gt;</b> - Serves user/Citymonstret, but not user/ or user/Citymonstret/other
 * </p>
 * <p>
 *     <b>news/[page]</b> - Serves news, news/1, news/foo, but not news/foo/bar
 * </p>
 * <p>
 *     <b>user/&lt;username&gt;/posts/[page]</b> - Serves user/Citymonstret/posts and user/Citymonstrst/posts/10
 * </p>
 */
@SuppressWarnings("unused")
public class ViewPattern
{

    // Compiler variable: Show debug messages
    private static final boolean debug = false;

    private final List<Part> parts;
    private final String raw;

    /**
     * Generate a list of parts from the provided string
     * @param in String to compile
     */
    public ViewPattern(final String in)
    {
        Assert.notNull( in );

        this.raw = in;
        final SmartString string = new SmartString( raw.toLowerCase() );
        string.replaceLastIf( '/', SmartString.nil );
        this.parts = new ArrayList<>();
        boolean openOptional = false;
        boolean openRequired = false;
        boolean first = true;
        String name = "";
        for ( final char c : string )
        {
            if ( c == '<' )
            {
                if ( !name.isEmpty() )
                {
                    parts.add( new Static( name ) );
                }
                openRequired = true;
                name = "";
            } else if ( c == '[' )
            {
                if ( !name.isEmpty() )
                {
                    parts.add( new Static( name ) );
                }
                openOptional = true;
                name = "";
            } else if ( openRequired && c == '>' )
            {
                openRequired = false;
                parts.add( new Variable( name, Variable.TYPE_REQUIRED ) );
                name = "";
            } else if ( openOptional && c == ']' )
            {
                openOptional = false;
                parts.add( new Variable( name, Variable.TYPE_OPTIONAL ) );
                name = "";
            } else if ( c == '/' )
            {
                if ( !name.isEmpty() )
                {
                    parts.add( new Static( name ) );
                    name = "";
                }
                parts.add( new Split() );
            } else
            {
                name += c;
            }
        }
        if ( !name.isEmpty() )
        {
            parts.add( new Static( name ) );
        }
    }

    /**
     * Test if a string matches the pattern
     * @param in String to test for
     * @return A map containing the variables extracted from the string.
     *         If there was no match, the map will be null
     */
    public Map<String, String> matches(final String in)
    {
        Assert.notNull( in );

        final SmartString url;
        if ( in.contains( "?" ) )
        {
            url = new SmartString( in.split( "\\?" )[ 0 ] );
        } else
        {
            url = new SmartString( in );
        }

        url.replaceIf( 0, '/', SmartString.nil );
        url.replaceLastIf( '/', SmartString.nil );

        if ( parts.isEmpty() )
        {
            if ( url.toString().isEmpty() )
            {
                return new HashMap<>();
            } else
            {
                return null;
            }
        }

        final String[] p = url.toString().split( "((?<=/)|(?=/))" );
        final List<String> finalList = new ArrayList<>();
        for ( final String l : p )
        {
            if ( l.contains( "." ) )
            {
                final String[] k = l.split( "(?=\\.)" );
                finalList.add( k[ 0 ] );
                finalList.add( k[ 1 ] );
            } else
            {
                finalList.add( l );
            }
        }

        final Map<String, String> variables = new HashMap<>();
        final Iterator<String> stringIterator = finalList.iterator();

        Part lastPart = null;

        final List<Part> passed = new ArrayList<>();

        for ( final Part part : parts )
        {

            boolean has = stringIterator.hasNext();
            String next = has ? stringIterator.next() : "";

            if ( part instanceof Variable )
            {
                Variable v = (Variable) part;
                if ( v.getType() == Variable.TYPE_REQUIRED )
                {
                    if ( !has )
                    {
                        if ( debug )
                        {
                            ServerImplementation.getImplementation().log( "Missing required type: " + part );
                        }
                        return null;
                    }
                }
            } else if ( part instanceof Static )
            {
                if ( !has )
                {
                    if ( debug )
                    {
                        ServerImplementation.getImplementation().log( "Missing static type: " + part );
                    }
                    return null;
                } else
                {
                    if ( !next.equalsIgnoreCase( part.toString() ) )
                    {
                        if ( debug )
                        {
                            ServerImplementation.getImplementation().log( "Non-Matching static type: " + part + " | " + next + " | " +
                                    this.parts + " | " + url + " | " + passed );
                        }
                        return null;
                    }
                }
            }

            if ( part instanceof Variable )
            {
                final Variable variable = (Variable) part;
                variables.put( variable.getName(), next );
            }

            passed.add( part );
        }
        if ( stringIterator.hasNext() )
        {
            if ( debug )
            {
                ServerImplementation.getImplementation().log( "Too Many: " + stringIterator.next() + " | " +
                        this.parts + " | " + url + " | " + passed + " | " + this.raw );
            }
            return null;
        }
        return variables;
    }

    @Override
    public String toString()
    {
        return this.raw;
    }

    private abstract static class Part
    {

        @Override
        public abstract String toString();

    }

    private static class Static extends Part
    {

        private final String string;

        private Static(final String string)
        {
            this.string = string;
        }

        @Override
        public String toString()
        {
            return string;
        }
    }

    @SuppressWarnings("unused")
    private static class SmartString implements Iterable<Character>
    {

        private static final char nil = '#';

        private char[] chars;
        private int length;

        private boolean changed = false;

        private SmartString(final String in)
        {
            Assert.notNull( in );

            this.chars = in.toCharArray();
            this.length = in.length();
        }

        char lastCharacter()
        {
            return chars[ length - 1 ];
        }

        void replaceLast(char c)
        {
            set( length - 1, c );
        }

        void replaceAll(char c, char w)
        {
            int[] indices = findAll( c );
            for ( int i : indices )
            {
                set( i, w );
            }
        }

        void replaceLastIf(char c, char k)
        {
            replaceIf( length - 1, c, k );
        }

        void replaceIf(int n, char c, char k)
        {
            if ( length == 0 )
            {
                return;
            }
            if ( chars[ n ] == c )
            {
                set( n, k );
            }
        }

        int[] findAll(char c)
        {
            int[] indices = new int[ length ];
            int written = 0;
            for ( int i = 0; i < length; i++ )
            {
                if ( chars[ i ] == c )
                {
                    indices[ written++ ] = i;
                }
            }
            int[] n = new int[ written ];
            System.arraycopy( indices, 0, n, 0, n.length );
            return n;
        }

        void set(final int i, final char c)
        {
            if ( length == 0 )
            {
                return;
            }
            chars[ i ] = c;
            changed = true;
        }

        void remove(final int i)
        {
            set( i, nil );
        }

        void regenerate()
        {
            char[] temp = new char[ length ];
            int index = 0;
            for ( char c : chars )
            {
                if ( c != nil )
                {
                    temp[ index++ ] = c;
                }
            }
            chars = new char[ index ];
            System.arraycopy( temp, 0, chars, 0, index );
            length = chars.length;
            changed = false;
        }

        @Override
        public String toString()
        {
            if ( changed )
            {
                regenerate();
            }
            return new String( chars );
        }

        @Override
        public Iterator<Character> iterator()
        {
            return new Iterator<Character>()
            {

                int index = 0;

                {
                    SmartString.this.regenerate();
                }

                @Override
                public boolean hasNext()
                {
                    return index < length;
                }

                @Override
                public Character next()
                {
                    return chars[ index++ ];
                }
            };
        }
    }

    private static final class Split extends Part
    {

        @Override
        public String toString()
        {
            return "/";
        }
    }

    private static class Variable extends Part
    {

        private static int TYPE_REQUIRED = 0, TYPE_OPTIONAL = 1;
        private final String name;
        private final int type;

        Variable(String name, int type)
        {
            this.name = name;
            this.type = type;
        }

        public String getName()
        {
            return name;
        }

        public int getType()
        {
            return type;
        }

        @Override
        public String toString()
        {
            return this.name + ( type == TYPE_REQUIRED ? "" : "?" );
        }
    }

}
