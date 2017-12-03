/*
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
package xyz.kvantum.server.implementation;

import lombok.Getter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.RequestCompiler;

import java.io.InputStream;
import java.util.Optional;

/**
 * Read a HTTP request until the first clear line. Does
 * not read the HTTP message. The reader uses {@link java.nio.charset.StandardCharsets#US_ASCII}
 * as the charset, as defined by the HTTP protocol.
 */
@SuppressWarnings({ "unused", "WeakerAccess" })
final class RequestReader
{

    @Getter
    private final StringBuilder builder;
    private final AbstractRequest abstractRequest;
    private char lastCharacter = ' ';
    private boolean done = false;
    private boolean begunLastLine = false;
    private boolean hasQuery = false;

    RequestReader(final AbstractRequest abstractRequest)
    {
        this.abstractRequest = abstractRequest;
        this.builder = new StringBuilder( CoreConfig.Limits.limitRequestLineSize );
    }

    /**
     * Check whether the reader is done reading the request
     *
     * @return true if the request is read, false if not
     */
    boolean isDone()
    {
        return this.done;
    }

    /**
     * Get the last read character, ' ' if no character has been read
     *
     * @return last read character
     */
    public char getLastCharacter()
    {
        return this.lastCharacter;
    }

    /**
     * Attempt to read a byte
     *
     * @param b Byte
     * @return True if the byte was read, False if not
     */
    boolean readByte(final byte b)
    {
        if ( done )
        {
            return false;
        }

        final char character = (char) b;

        if ( begunLastLine )
        {
            if ( character == '\n' )
            {
                return ( done = true );
            } else
            {
                begunLastLine = false;
            }
        }

        if ( lastCharacter == '\r' )
        {
            if ( character == '\n' )
            {
                if ( builder.length() != 0 )
                {
                    final String line = builder.toString();
                    if ( !hasQuery )
                    {
                        RequestCompiler.compileQuery( this.abstractRequest, line );
                        hasQuery = true;
                    } else
                    {
                        final Optional<RequestCompiler.HeaderPair> headerPair = RequestCompiler.compileHeader( line );
                        if ( headerPair.isPresent() )
                        {
                            final RequestCompiler.HeaderPair pair = headerPair.get();
                            this.abstractRequest.getHeaders().put( pair.getKey(), pair.getValue() );
                        } else
                        {
                            Logger.warn( "Failed to read request line: '%s'", line );
                        }
                    }
                }
                builder.setLength( 0 );
            }
        } else
        {
            if ( lastCharacter == '\n' && character == '\r' )
            {
                begunLastLine = true;
            } else if ( character != '\n' && character != '\r' )
            {
                builder.append( character );
            }
        }
        lastCharacter = character;
        return true;
    }

    /**
     * Attempt to read the integer as a byte,
     * wrapper method for usage with {@link InputStream#read()}
     *
     * @param val Integer (byte)
     * @return true if the byte was read, false if not
     */
    boolean readByte(final int val)
    {
        if ( val == -1 )
        {
            this.done = true;
            return false;
        }
        return this.readByte( (byte) val );
    }

    /**
     * Attempt to read a byte array
     *
     * @param bytes Byte array
     * @return Number of read bytes
     */
    int readBytes(final byte[] bytes)
    {
        return this.readBytes( bytes, bytes.length );
    }

    /**
     * Attempt to read a byte array
     *
     * @param bytes  Byte array
     * @param length Length to be read
     * @return Number of read bytes
     */
    int readBytes(final byte[] bytes, final int length)
    {
        int read = 0;
        for ( int i = 0; i < length && i < bytes.length; i++ )
        {
            if ( this.readByte( bytes[ i ] ) )
            {
                read++;
            }
            if ( this.done )
            {
                break;
            }
        }
        return read;
    }

    /**
     * Clear the request reader
     */
    public void clear()
    {
        this.builder.setLength( 0 );
        this.lastCharacter = ' ';
    }

}
