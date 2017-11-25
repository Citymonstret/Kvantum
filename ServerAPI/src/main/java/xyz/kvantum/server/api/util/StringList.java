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
package xyz.kvantum.server.api.util;

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
