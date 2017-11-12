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
package com.github.intellectualsites.kvantum.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Represents a list of strings, that is stored inside of a string,
 * with the format: string1,string2,string3,string4...
 */
final public class StringList implements Iterable<String>
{

    private final Collection<String> content;

    /**
     * Initialize a new string list
     *
     * @param string Initial content
     */
    public StringList(final String string)
    {
        this.content = new ArrayList<>();
        final StringTokenizer tokenizer = new StringTokenizer( string, "," );
        while ( tokenizer.hasMoreTokens() )
        {
            this.content.add( tokenizer.nextToken() );
        }
    }

    /**
     * Remove an item from the list
     *
     * @param string Item to be removed
     * @return True if the item was removed, else false
     */
    public boolean remove(final String string)
    {
        return this.content.remove( string );
    }

    /**
     * Add an item to the list
     *
     * @param string Item to be added
     * @return True if the item was added, else false
     */
    public boolean add(final String string)
    {
        return this.content.add( string );
    }

    /**
     * Check if the list contains an item
     *
     * @param string Item
     * @return True if the list contains the item, else false
     */
    public boolean contains(final String string)
    {
        return this.content.contains( string );
    }

    /**
     * Convert the list to a string, joining with ","
     * @return Joined list
     */
    @Override
    public String toString()
    {
        return CollectionUtil.join( this.content, "," );
    }

    /**
     * Provide the item iterator. Delegate for {@link ArrayList#iterator()}
     * @return list iterator
     */
    @Override
    public Iterator<String> iterator()
    {
        return this.content.iterator();
    }
}
