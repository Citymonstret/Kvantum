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
package xyz.kvantum.crush.syntax;

import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.util.ProviderFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is used to create Crush syntax
 * such as {{variable.name}} or the foreach loop.
 * <p>
 * This has to be registered by the server in order to be used
 *
 * @author Citymonstret
 */
public abstract class Syntax
{

    private final Pattern pattern;

    /**
     * Constructor
     *
     * @param pattern The regex pattern used to match the code
     */
    public Syntax(final Pattern pattern)
    {
        this.pattern = pattern;
    }

    /**
     * Process the input string
     *
     * @param in        Code Input
     * @param matcher   RegEx ViewMatcher
     * @param r         HTML Request
     * @param factories Provider Factories
     * @return Processed string
     * @see #handle(String, AbstractRequest, Map) Simple wrapper
     */
    public abstract String process(String in, Matcher matcher, AbstractRequest r, Map<String, ProviderFactory> factories);

    /**
     * A simple wrapper for the process method
     *
     * @param in        String input
     * @param r         HTML Request
     * @param factories Provider Factories
     * @return Processed Input
     * @see #process(String, Matcher, AbstractRequest, Map) Wraps around this
     */
    public final String handle(final String in, final AbstractRequest r, final Map<String, ProviderFactory> factories)
    {
        return process( in, pattern.matcher( in ), r, factories );
    }

    /**
     * Check if the regex pattern matches
     *
     * @param in Code Input
     * @return True if the regex matches
     */
    public final boolean matches(final String in)
    {
        return this.pattern.matcher( in ).find();
    }

    /**
     * Get the identifier pattern object
     *
     * @return Pattern
     */
    public Pattern getPattern()
    {
        return this.pattern;
    }
}
