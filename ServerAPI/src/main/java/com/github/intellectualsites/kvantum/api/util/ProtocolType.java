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
package com.github.intellectualsites.kvantum.api.util;

import java.util.Locale;
import java.util.Optional;

/**
 * Protocol implementation enum
 */
public enum ProtocolType
{

    HTTP,
    HTTPS;

    /**
     * Match a string to a {@link ProtocolType}, if possible
     * @param string String to match, may not be null
     * @return matched protocol type if found
     */
    public static Optional<ProtocolType> getByName(final String string)
    {
        Assert.notEmpty( string );

        final String fixed = string.replaceAll( "\\s", "" ).toUpperCase( Locale.ENGLISH );
        return LambdaUtil.getFirst( values(), type -> type.name().equals( fixed ) );
    }

}
