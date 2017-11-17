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
package com.github.intellectualsites.kvantum.api.fileupload;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.request.HttpMethod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload.UploadContext;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wrapper for {@link UploadContext}
 */
@SuppressWarnings("all")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final public class KvantumFileUploadContext implements UploadContext
{

    private final AbstractRequest request;
    private final InputStream inputStream;

    /**
     * Try to generate a new {@link KvantumFileUploadContext} for a {@link AbstractRequest}.
     * This method will verify that the request has supplied a multipart/form-data body,
     * that the content lenght is supplied (and that it agrees with the data length) and that
     * the content length is within the limit specified by
     * {@link com.github.intellectualsites.kvantum.api.config.CoreConfig.Limits#limitMultibodySize}.
     * It will also provide a wrapping inputstream that prevents any reading implementations from
     * attempting to read beyond the request.
     *
     * @param request Incoming request
     * @return Parsing result
     */
    public static KvantumFileUploadContextParsingResult from(final AbstractRequest request)
    {
        if ( request.getQuery().getMethod() == HttpMethod.POST && request.getHeader( "Content-Type" )
                .startsWith( "multipart" ) )
        {
            int suppliedContentLength;
            try
            {
                suppliedContentLength = Integer
                        .parseInt( request.getHeader( "content-length" ) );
            } catch ( final Exception e )
            {
                return new KvantumFileUploadContextParsingResult( KvantumFileUploadContextParsingStatus
                        .BAD_CONTENT_LENGTH_HEADER, null );
            }

            final BufferedReader input = request.getInputReader();
            final StringBuilder stringBuilder = new StringBuilder();
            String line;
            int read = 0;
            try
            {
                while ( ( line = input.readLine() ) != null )
                {
                    final String lineWithNewline = line + "\r\n";
                    read += ( lineWithNewline ).getBytes().length;
                    if ( read >= CoreConfig.Limits.limitPostMultipartSize )
                    {
                        return new KvantumFileUploadContextParsingResult( KvantumFileUploadContextParsingStatus
                                .ENTITY_TOO_LARGE, null );
                    }
                    stringBuilder.append( lineWithNewline );
                    if ( line.endsWith( "--" ) )
                    {
                        break;
                    }
                }
            } catch ( final IOException e )
            {
                if ( CoreConfig.debug )
                {
                    e.printStackTrace();
                }
                return new KvantumFileUploadContextParsingResult( KvantumFileUploadContextParsingStatus.ERROR,
                        null );
            }
            if ( read != suppliedContentLength )
            {
                if ( CoreConfig.debug )
                {
                    Logger.debug( "Client supplied content length '%s', but actual content length was: '%s'",
                            suppliedContentLength, read );
                }
                return new KvantumFileUploadContextParsingResult( KvantumFileUploadContextParsingStatus
                        .CONTENT_LENGTH_MISMATCH, null );
            }

            final KvantumFileUploadContext context = new KvantumFileUploadContext( request,
                    new ByteArrayInputStream( stringBuilder.toString().getBytes( StandardCharsets.UTF_8 ) ) );
            return new KvantumFileUploadContextParsingResult( KvantumFileUploadContextParsingStatus.SUCCESS, context );
        }
        return new KvantumFileUploadContextParsingResult( KvantumFileUploadContextParsingStatus
                .BAD_CONTENT_TYPE, null );
    }

    @Override
    public String getCharacterEncoding()
    {
        return "UTF-8";
    }

    @Override
    public String getContentType()
    {
        return request.getHeader( "Content-Type" );
    }

    @Override
    public int getContentLength()
    {
        return (int) contentLength();
    }

    @Override
    public long contentLength()
    {
        return Long.parseLong( this.request.getHeader( "content-length" ) );
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return this.inputStream;
    }

    /**
     * Request parsing status
     */
    public enum KvantumFileUploadContextParsingStatus
    {
        /**
         * Content parsed successfully
         */
        SUCCESS,
        /**
         * Content length header is not a number
         */
        BAD_CONTENT_LENGTH_HEADER,
        /**
         * Content-type is not multipart
         */
        BAD_CONTENT_TYPE,
        /**
         * Supplied content length, and actual body lenght do not agree
         */
        CONTENT_LENGTH_MISMATCH,
        /**
         * The supplied entity body is too large
         */
        ENTITY_TOO_LARGE,
        /**
         * Something went wrong when parsing the request
         */
        ERROR
    }

    /**
     * Request parsing result
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class KvantumFileUploadContextParsingResult
    {

        private final KvantumFileUploadContextParsingStatus status;
        private final KvantumFileUploadContext context;
    }

}
