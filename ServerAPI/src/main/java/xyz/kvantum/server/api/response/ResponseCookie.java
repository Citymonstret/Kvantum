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
package xyz.kvantum.server.api.response;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.kvantum.server.api.util.TimeUtil;

import java.util.Date;

@EqualsAndHashCode
@Getter
@Builder
public final class ResponseCookie
{

    @Builder.Default
    public String path = "/";
    private String cookie;
    private String value;
    @Builder.Default
    private Date expires = null;
    @Builder.Default
    private boolean httpOnly = false;
    @Builder.Default
    private boolean secure = false;

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder( this.cookie )
                .append( "=" ).append( value ).append( "; Path=" ).append( path );
        if ( this.expires != null )
        {
            builder.append( "; Expires=" ).append( TimeUtil.getHTTPTimeStamp( this.expires ) );
        }
        if ( this.secure )
        {
            builder.append( "; Secure" );
        }
        if ( this.httpOnly )
        {
            builder.append( "; HttpOnly" );
        }
        return builder.toString();
    }

}
