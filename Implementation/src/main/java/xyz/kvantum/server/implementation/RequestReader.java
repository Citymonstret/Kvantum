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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.RequestCompiler;
import xyz.kvantum.server.api.request.post.DummyPostRequest;
import xyz.kvantum.server.api.request.post.EntityType;
import xyz.kvantum.server.api.request.post.JsonPostRequest;
import xyz.kvantum.server.api.request.post.MultipartPostRequest;
import xyz.kvantum.server.api.request.post.UrlEncodedPostRequest;
import xyz.kvantum.server.api.response.Header;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Read a HTTPabstractRequest until the first clear line. Does
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
    private ByteBuf overloadBuffer;
    private int contentLength = -1;

    RequestReader(final AbstractRequest abstractRequest)
    {
        this.abstractRequest = abstractRequest;
        this.builder = new StringBuilder( CoreConfig.Limits.limitRequestLineSize );
    }

    /**
     * Check whether the reader is done reading theabstractRequest
     *
     * @return true if theabstractRequest is read, false if not
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
    boolean readByte(final byte b) throws Throwable
    {
        if ( done )
        {
            return false;
        }

        if ( contentLength != -1 )
        {
            if ( overloadBuffer.readableBytes() >= contentLength )
            {
                return false;
            }
            this.overloadBuffer.writeByte( b );
            if ( overloadBuffer.readableBytes() == contentLength )
            {
                final String contentType = abstractRequest.getHeader( "Content-Type" );
                boolean isFormURLEncoded;

                if ( ( isFormURLEncoded = contentType.startsWith( "application/x-www-form-urlencoded" ) ) ||
                        ( EntityType.JSON.getContentType().startsWith( contentType ) ) )
                {
                    try
                    {
                        final String content = overloadBuffer.readCharSequence( contentLength,
                                StandardCharsets.UTF_8 ).toString();

                        if ( isFormURLEncoded )
                        {
                            abstractRequest.setPostRequest( new UrlEncodedPostRequest( abstractRequest, content ) );
                        } else
                        {
                            abstractRequest.setPostRequest( new JsonPostRequest( abstractRequest, content ) );
                        }
                    } catch ( final Exception e )
                    {
                        Logger.warn( "Failed to read url encoded postAbstractRequest (Request: %s): %s",
                                abstractRequest, e.getMessage() );
                    }
                } else if ( contentType.startsWith( "multipart" ) )
                {
                    byte[] bytes = new byte[ contentLength ];
                    overloadBuffer.readBytes( bytes );
                    abstractRequest.setOverloadBytes( bytes );
                    abstractRequest.setPostRequest( new MultipartPostRequest( abstractRequest, "" ) );
                } else
                {
                    Logger.warn( "Request provided unknown postabstractRequest type (Request: %s): %s", abstractRequest,
                            contentType );
                    abstractRequest.setPostRequest( new DummyPostRequest( abstractRequest, "" ) );
                }

                this.done = true;
            }
            return true;
        }

        final char character = (char) b;

        if ( begunLastLine )
        {
            if ( character == '\n' )
            {
                final String contentLength = abstractRequest.getHeader( "content-length" );
                if ( contentLength.isEmpty() )
                {
                    return ( done = true );
                }
                try
                {
                    this.contentLength = Integer.parseInt( contentLength );
                } catch ( final Exception e )
                {
                    throw new ReturnStatus( Header.STATUS_BAD_REQUEST, null );
                }
                this.overloadBuffer = ByteBufAllocator.DEFAULT.buffer( this.contentLength );
                if ( this.contentLength >= CoreConfig.Limits.limitPostBasicSize )
                {
                    if ( CoreConfig.debug )
                    {
                        Logger.debug( "Supplied post body size too large (%s > %s)", contentLength,
                                CoreConfig.Limits.limitPostBasicSize );
                    }
                    throw new ReturnStatus( Header.STATUS_ENTITY_TOO_LARGE, null );
                }
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
                            Logger.warn( "Failed to readabstractRequest line: '%s'", line );
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
    boolean readByte(final int val) throws Throwable
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
    int readBytes(final byte[] bytes) throws Throwable
    {
        return this.readBytes( bytes, bytes.length );
    }

    int readBytes(final ByteBuf byteBuf) throws Throwable
    {
        final int length = byteBuf.readableBytes();

        final byte[] bytes = new byte[ length ];
        byteBuf.readBytes( bytes, 0, length );

        return this.readBytes( bytes, length );
    }

    /**
     * Attempt to read a byte array
     *
     * @param bytes  Byte array
     * @param length Length to be read
     * @return Number of read bytes
     */
    int readBytes(final byte[] bytes, final int length) throws Throwable
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
     * Clear theabstractRequest reader
     */
    public void clear()
    {
        this.builder.setLength( 0 );
        this.lastCharacter = ' ';
        this.overloadBuffer.release();
        this.contentLength = -1;
    }

}
