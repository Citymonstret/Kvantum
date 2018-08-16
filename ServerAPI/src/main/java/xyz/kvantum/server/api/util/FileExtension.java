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
package xyz.kvantum.server.api.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import xyz.kvantum.server.api.response.Header;

public enum FileExtension
{
	CSS( new String[] { "css", }, Header.CONTENT_TYPE_CSS, "/* {cmt} */" ), HTML(
		new String[] { "html", "xhtml", "htm", "vm" }, Header.CONTENT_TYPE_HTML, "<!-- {cmt} -->" ), PNG(
		new String[] { "png" }, AsciiString.of( "image/png; charset=utf-8" ), "png", ReadType.BYTES, "" ), ICO(
		new String[] { "ico" }, AsciiString.of( "image/x-icon; charset=utf-8" ), "x-icon", ReadType.BYTES, "" ), GIF(
		new String[] { "gif" }, AsciiString.of( "image/gif; charset=utf-8" ), "gif", ReadType.BYTES, "" ), JPEG(
		new String[] { "jpg", "jpeg" }, AsciiString.of( "image/jpeg; charset=utf-8" ), "jpeg", ReadType.BYTES,
		"" ), ZIP( new String[] { "zip" }, Header.CONTENT_TYPE_OCTET_STREAM, "zip", ReadType.BYTES, "" ), TXT(
		new String[] { "txt" }, Header.CONTENT_TYPE_OCTET_STREAM, "txt", ReadType.BYTES, "" ), PDF(
		new String[] { "pdf" }, Header.CONTENT_TYPE_OCTET_STREAM, "pdf", ReadType.BYTES, "" ), JAVASCRIPT(
		new String[] { "js", }, Header.CONTENT_TYPE_JAVASCRIPT, "/* {cmt} */" );

	public static final List<FileExtension> IMAGE = Collections
			.unmodifiableList( Arrays.asList( PNG, ICO, GIF, JPEG ) );
	public static final List<FileExtension> DOWNLOADABLE = Collections
			.unmodifiableList( Arrays.asList( PDF, TXT, ZIP ) );
	private final String option;
	private final Collection<String> extensions;
	private final String extension;
	private final AsciiString contentType;
	private final ReadType readType;
	private final String comment;

	FileExtension(final String[] extensions, final AsciiString contentType, final String comment)
	{
		this.extensions = CollectionUtil.arrayToCollection( HashSet::new, extensions );
		this.extension = extensions[ 0 ];
		this.contentType = contentType;
		this.option = "";
		this.readType = ReadType.TEXT;
		this.comment = comment;
	}

	FileExtension(final String[] extensions, final AsciiString contentType, final String option,
			final ReadType readType, final String comment)
	{
		this.extensions = CollectionUtil.arrayToCollection( HashSet::new, extensions );
		this.extension = extensions[ 0 ];
		this.contentType = contentType;
		this.option = option;
		this.readType = readType;
		this.comment = comment;
	}

	public static Optional<FileExtension> getExtension(final String string)
	{
		String workingString = string;
		if ( string.startsWith( "." ) )
		{
			workingString = string.substring( 1 );
		}
		for ( final FileExtension extension : values() )
		{
			if ( CollectionUtil.containsIgnoreCase( extension.extensions, workingString ) )
			{
				return Optional.of( extension );
			}
		}
		return Optional.empty();
	}

	public String getComment(final String comment)
	{
		return this.comment.replace( "{cmt}", comment );
	}

	public ReadType getReadType()
	{
		return readType;
	}

	public AsciiString getContentType()
	{
		return contentType;
	}

	public String getOption()
	{
		return option;
	}

	public boolean isImage()
	{
		return IMAGE.contains( this );
	}

	public boolean matches(final String string)
	{
		String workingString = string;
		if ( workingString.startsWith( "." ) )
		{
			workingString = workingString.substring( 1 );
		}
		for ( final String e : extensions )
		{
			if ( e.equalsIgnoreCase( workingString ) )
			{
				return true;
			}
		}
		return false;
	}

	public String getExtension()
	{
		return this.extension;
	}

	public enum ReadType
	{
		TEXT, BYTES
	}
}
