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

import xyz.kvantum.server.api.response.Header;

import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public enum FileType
{
    HTML( "html", Header.CONTENT_TYPE_HTML ),
    CSS( "css", Header.CONTENT_TYPE_CSS ),
    JAVASCRIPT( "js", Header.CONTENT_TYPE_JAVASCRIPT );

    private final String extension;

    private final String contentType;

    FileType(String extension, String contentType)
    {
        this.extension = extension;
        this.contentType = contentType;
    }

    public static Optional<FileType> byExtension(final String ext)
    {
        Assert.notNull( ext );

        final Predicate<FileType> filter = type -> type.extension.equalsIgnoreCase( ext );
        return LambdaUtil.getFirst( values(), filter );
    }

    public String getExtension()
    {
        return extension;
    }

    public String getContentType()
    {
        return contentType;
    }

}
