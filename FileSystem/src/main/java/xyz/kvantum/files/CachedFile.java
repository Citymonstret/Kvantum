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
package xyz.kvantum.files;

import java.nio.charset.StandardCharsets;

public final class CachedFile
{

    private final byte[] bytes;

    CachedFile(final String content)
    {
        if ( content == null )
        {
            throw new NullPointerException( "Given string content was null" );
        }
        this.bytes = content.getBytes( StandardCharsets.UTF_8 );
    }

    CachedFile(final byte[] bytes)
    {
        if ( bytes == null )
        {
            throw new NullPointerException( "Given byte[] content was null" );
        }
        this.bytes = bytes;
    }

    @Override
    public String toString()
    {
        return this.getAsString();
    }

    String getAsString()
    {
        return new String( bytes, StandardCharsets.UTF_8 );
    }

    byte[] getAsByteArray()
    {
        return this.bytes;
    }
}
