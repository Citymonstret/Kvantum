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
package xyz.kvantum.server.api.util;

import com.google.common.base.Charsets;
import com.google.common.collect.HashBiMap;
import lombok.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility object for dealing with US-ASCII encoded strings.
 * The strings are immutable, and can thus safely
 * be reused in multiple contexts.
 */
@SuppressWarnings("unused")
public final class AsciiString implements CharSequence, AsciiStringable, Comparable<CharSequence>
{

    private static final Map<String, AsciiString> map = HashBiMap.create();
    public static final AsciiString empty = of( "" );
    private final byte[] value;
    private final String string;
    private final boolean lowercase;
    private final boolean uppercase;
    private final int hashCode;

    private AsciiString(@NonNull final String value)
    {
        this.value = value.getBytes( Charsets.US_ASCII );
        this.string = value;
        this.hashCode = this.string.hashCode();
        this.lowercase = this.string.toLowerCase( Locale.ENGLISH ).equals( this.string );
        this.uppercase = this.string.toUpperCase( Locale.ENGLISH ).equals( this.string );
    }

    private AsciiString(@NonNull final byte[] value)
    {
        this.value = value;
        this.string = new String( value, StandardCharsets.US_ASCII );
        this.hashCode = this.string.hashCode();
        this.lowercase = this.string.toLowerCase( Locale.ENGLISH ).equals( this.string );
        this.uppercase = this.string.toUpperCase( Locale.ENGLISH ).equals( this.string );
    }

    public static AsciiString randomUUIDAsciiString()
    {
        return of( UUID.randomUUID().toString(), false );
    }

    /**
     * Create a new (cached) ascii string or retrieve
     * the object from the cache
     *
     * @param string String value
     * @return Created (or retrieved from cache) string
     */
    public static AsciiString of(final String string)
    {
        return of( string, true );
    }

    public static AsciiString of(@NonNull final String string,
                                 final boolean cache)
    {
        if ( map.containsKey( string ) )
        {
            return map.get( string );
        }
        final AsciiString asciiString = new AsciiString( string );
        if ( cache )
        {
            map.put( string, asciiString );
        }
        return asciiString;
    }

    public static AsciiString of(final byte[] string)
    {
        return new AsciiString( string );
    }

    public boolean isEmpty()
    {
        return this.length() == 0;
    }

    /**
     * Get the byte array backing the string
     *
     * @return Bytes
     */
    public byte[] getValue()
    {
        return this.value;
    }

    @Override
    public int length()
    {
        return this.string.length();
    }

    @Override
    public char charAt(final int i)
    {
        return this.string.charAt( i );
    }

    @Override
    public CharSequence subSequence(final int i, final int i1)
    {
        return this.string.subSequence( i, i1 );
    }

    @Override
    @SuppressWarnings("ALL")
    public String toString()
    {
        return this.string;
    }

    @Override
    public int hashCode()
    {
        return this.hashCode;
    }

    @Override
    public boolean equals(final Object object)
    {
        if ( object instanceof AsciiString )
        {
            return this.string.equals( ( (AsciiString) object ).string );
        } else if ( object instanceof String )
        {
            return this.string.equals( object );
        } else
        {
            return object instanceof byte[] && this.value.equals( object );
        }
    }

    /**
     * Delegate for {@link String#contains(CharSequence)}
     */
    public boolean contains(@NonNull final CharSequence other)
    {
        return this.string.contains( other );
    }

    @SuppressWarnings("ALL")
    public boolean containsIgnoreCase(@NonNull final CharSequence other)
    {
        final String localString;
        final String otherString;
        if ( lowercase )
        {
            localString = this.string;
            otherString = other.toString().toLowerCase( Locale.ENGLISH );
        } else if ( uppercase )
        {
            localString = this.string;
            otherString = other.toString().toUpperCase( Locale.ENGLISH );
        } else
        {
            localString = this.string.toLowerCase( Locale.ENGLISH );
            otherString = other.toString().toLowerCase( Locale.ENGLISH );
        }
        return localString.contains( otherString );
    }

    public boolean equals(@NonNull final CharSequence other)
    {
        return this.string.equals( other );
    }

    public boolean equals(@NonNull final AsciiString other)
    {
        return Arrays.equals( this.value, other.value );
    }

    @SuppressWarnings("ALL")
    public boolean equalsIgnoreCase(@NonNull final CharSequence other)
    {
        if ( this == other )
        {
            return true;
        }
        final String localString;
        final String otherString;
        if ( lowercase )
        {
            localString = this.string;
            otherString = other.toString().toLowerCase( Locale.ENGLISH );
        } else if ( uppercase )
        {
            localString = this.string;
            otherString = other.toString().toUpperCase( Locale.ENGLISH );
        } else
        {
            localString = this.string.toLowerCase( Locale.ENGLISH );
            otherString = other.toString().toLowerCase( Locale.ENGLISH );
        }
        return localString.equals( otherString );
    }

    /**
     * Delegate for {@link String#endsWith(String)}
     */
    public boolean endsWith(@NonNull final String string)
    {
        return this.string.endsWith( string );
    }

    public AsciiString toLowerCase()
    {
        if ( this.lowercase )
        {
            return this;
        }
        return of( this.string.toLowerCase( Locale.ENGLISH ), false );
    }

    public AsciiString toUpperCase()
    {
        if ( this.uppercase )
        {
            return this;
        }
        return of( this.string.toUpperCase( Locale.ENGLISH ), false );
    }

    @Override
    public AsciiString toAsciiString()
    {
        return this;
    }

    @Override
    public int compareTo(@NonNull final CharSequence sequence)
    {
        if ( this == sequence || sequence.equals( this ) )
        {
            return 0;
        }
        return this.string.compareTo( sequence.toString() );
    }

    public List<AsciiString> split(@NonNull final String delimiter)
    {
        return Arrays.stream( this.string.split( delimiter ) ).map( string -> AsciiString.of( string, false ) )
                .collect( Collectors.toList() );
    }

    public boolean startsWith(@NonNull final String part)
    {
        return this.string.startsWith( part );
    }
}
