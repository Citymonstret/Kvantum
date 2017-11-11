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

    public StringList(final String string)
    {
        this.content = new ArrayList<>();
        final StringTokenizer tokenizer = new StringTokenizer( string, "," );
        while ( tokenizer.hasMoreTokens() )
        {
            this.content.add( tokenizer.nextToken() );
        }
    }

    public boolean remove(final String string)
    {
        return this.content.remove( string );
    }

    public boolean add(final String string)
    {
        return this.content.add( string );
    }

    public boolean contains(final String string)
    {
        return this.content.contains( string );
    }

    @Override
    public String toString()
    {
        return CollectionUtil.join( this.content, "," );
    }

    @Override
    public Iterator<String> iterator()
    {
        return this.content.iterator();
    }
}
