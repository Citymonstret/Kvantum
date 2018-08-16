/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.fileupload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload.UploadContext;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;

/**
 * Wrapper for {@link UploadContext}
 */
@SuppressWarnings("all") @RequiredArgsConstructor(access = AccessLevel.PRIVATE) final public class KvantumFileUploadContext
		implements UploadContext
{

	private final AbstractRequest request;
	private final InputStream inputStream;

	/**
	 * Try to generate a new {@link KvantumFileUploadContext} for a {@link AbstractRequest}. This method will verify
	 * that the request has supplied a multipart/form-data body, that the content lenght is supplied (and that it agrees
	 * with the data length) and that the content length is within the limit specified in server.yml
	 *
	 * It will also provide a wrapping inputstream that prevents any reading implementations from attempting to read
	 * beyond the request.
	 *
	 * @param request Incoming request
	 * @return Parsing result
	 */
	public static KvantumFileUploadContextParsingResult from(final AbstractRequest request)
	{
		if ( request.getQuery().getMethod() == HttpMethod.POST && request.getHeader( "Content-Type" )
				.startsWith( "multipart" ) )
		{
			final KvantumFileUploadContext context = new KvantumFileUploadContext( request,
					new ByteArrayInputStream( request.getOverloadBytes() ) );
			return new KvantumFileUploadContextParsingResult( KvantumFileUploadContextParsingStatus.SUCCESS, context );
		}
		return new KvantumFileUploadContextParsingResult( KvantumFileUploadContextParsingStatus.BAD_CONTENT_TYPE,
				null );
	}

	@Override public String getCharacterEncoding()
	{
		return "UTF-8";
	}

	@Override public String getContentType()
	{
		return request.getHeader( "Content-Type" ).toString();
	}

	@Override public int getContentLength()
	{
		return ( int ) contentLength();
	}

	@Override public long contentLength()
	{
		return this.request.getHeader( "content-length" ).toLong();
	}

	@Override public InputStream getInputStream() throws IOException
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
		SUCCESS, /**
	 * Content length header is not a number
	 */
	BAD_CONTENT_LENGTH_HEADER, /**
	 * Content-type is not multipart
	 */
	BAD_CONTENT_TYPE, /**
	 * Supplied content length, and actual body lenght do not agree
	 */
	CONTENT_LENGTH_MISMATCH, /**
	 * The supplied entity body is too large
	 */
	ENTITY_TOO_LARGE, /**
	 * Something went wrong when parsing the request
	 */
	ERROR
	}

	/**
	 * Request parsing result
	 */
	@Getter @RequiredArgsConstructor(access = AccessLevel.PRIVATE) public static class KvantumFileUploadContextParsingResult
	{

		private final KvantumFileUploadContextParsingStatus status;
		private final KvantumFileUploadContext context;
	}

}
