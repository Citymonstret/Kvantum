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
package xyz.kvantum.server.api.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.Cookie;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is an utility class
 * created to handle all
 * cookie related actions
 *
 * @author Citymonstret
 */
final public class CookieManager
{

    private static final Pattern PATTERN_COOKIE = Pattern.compile( "(?<key>[A-Za-z0-9_\\-]*)=" +
            "(?<value>.*)?" );
    private static final ListMultimap<String, Cookie> EMPTY_COOKIES = ArrayListMultimap.create( 0, 0 );

    /**
     * Get all cookies from a HTTP Request
     *
     * @param r HTTP Request
     * @return an array containing the cookies
     */
    public static ListMultimap<String, Cookie> getCookies(final AbstractRequest r)
    {
        Assert.isValid( r );

        String raw = r.getHeader( "Cookie" ).replaceAll( "\\s", "" );

        if ( raw.isEmpty() )
        {
            return EMPTY_COOKIES;
        }

        final ListMultimap<String, Cookie> cookies = MultimapBuilder.hashKeys().arrayListValues()
                .build();

        final StringTokenizer cookieTokenizer = new StringTokenizer( raw, ";" );
        while ( cookieTokenizer.hasMoreTokens() )
        {
            final String cookieString = cookieTokenizer.nextToken();

            final Matcher matcher = PATTERN_COOKIE.matcher( cookieString );
            if ( matcher.matches() )
            {
                if ( matcher.groupCount() < 2 )
                {
                    cookies.put( matcher.group( "key" ), new Cookie( matcher.group( "key" ), "" ) );
                } else
                {
                    cookies.put( matcher.group( "key" ), new Cookie( matcher.group( "key" ),
                            matcher.group( "value" ) ) );
                }
            }
        }

        return cookies;
    }

}
