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
package com.github.intellectualsites.kvantum.api.fileupload;

import com.github.intellectualsites.kvantum.api.request.Request;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload.UploadContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for {@link UploadContext}
 */
@RequiredArgsConstructor
final public class KvantumFileUploadContext implements UploadContext
{

    private final Request request;

    @Override
    public long contentLength()
    {
        return Long.parseLong( request.getHeader( "Content-Length" ) );
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
    public InputStream getInputStream() throws IOException
    {
        return request.getSocket().getSocket().getInputStream();
    }

}
