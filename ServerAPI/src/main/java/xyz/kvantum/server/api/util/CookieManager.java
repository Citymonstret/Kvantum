/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
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
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.Cookie;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is an utility class created to handle all request cookie related actions
 *
 * @author Citymonstret
 */
@UtilityClass public final class CookieManager {

    private static final Pattern PATTERN_COOKIE =
        Pattern.compile("(?<key>[A-Za-z0-9_\\-]*)=" + "(?<value>.*)?");
    private static final ListMultimap<AsciiString, Cookie> EMPTY_COOKIES =
        ArrayListMultimap.create(0, 0);
    private static final String CONST_COOKIE = "Cookie";
    private static final String CONST_SLASH_S = "\\s";
    private static final String CONST_EMPTY = "";
    private static final String CONST_SEMI_COLON = ";";
    private static final String CONST_KEY = "key";
    private static final String CONST_VALUE = "value";

    private static ListMultimap<AsciiString, Cookie> createNewMap() {
        return MultimapBuilder.hashKeys().arrayListValues().build();
    }

    /**
     * Get all cookies from a HTTP Request
     *
     * @param request HTTP Request
     * @return an array containing the cookies
     */
    public static ListMultimap<AsciiString, Cookie> getCookies(
        @NonNull final AbstractRequest request) {
        // Assert that the request is still valid
        Assert.isValid(request);
        // Extract the cookie header
        final String raw =
            request.getHeader(CONST_COOKIE).toString().replaceAll(CONST_SLASH_S, CONST_EMPTY);
        // Avoid unnecessary logic
        if (raw.isEmpty()) {
            return EMPTY_COOKIES;
        }
        // Create a new multimap
        final ListMultimap<AsciiString, Cookie> cookies = createNewMap();
        // Create a new tokenizer to extract the individual cookies
        final StringTokenizer cookieTokenizer = new StringTokenizer(raw, CONST_SEMI_COLON);
        // Loop through all the tokens
        while (cookieTokenizer.hasMoreTokens()) {
            final String cookieString = cookieTokenizer.nextToken();
            // Match the cookie to a regex (to extract the parts)
            final Matcher matcher = PATTERN_COOKIE.matcher(cookieString);
            if (matcher.matches()) {
                final AsciiString key = AsciiString.of(matcher.group(CONST_KEY));
                // Cookies don't necessarily have values
                final AsciiString value = matcher.groupCount() < 2 ?
                    AsciiString.empty :
                    AsciiString.of(matcher.group(CONST_VALUE), false);
                // Store the cookie in the map
                cookies.put(key, new Cookie(key, value));
            }
        }
        return cookies;
    }

}
