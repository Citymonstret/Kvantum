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
package com.github.intellectualsites.kvantum.api.response;

import com.github.intellectualsites.kvantum.api.config.CoreConfig;
import com.github.intellectualsites.kvantum.api.logging.Logger;
import com.github.intellectualsites.kvantum.api.util.Assert;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@EqualsAndHashCode(of = "text")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderOption
{

    private static Map<String, HeaderOption> headerOptionMap = new HashMap<>();

    @NonNull
    @Getter
    private final String text;
    @Getter
    private boolean cacheApplicable = true;

    public static HeaderOption create(final String text)
    {
        return create( text, true );
    }

    public static HeaderOption create(final String text, boolean cacheApplicable)
    {
        final HeaderOption headerOption = new HeaderOption( Assert.notNull( text ) ).cacheApplicable( cacheApplicable );
        headerOptionMap.put( text.toLowerCase(), headerOption );
        return headerOption;
    }

    public static HeaderOption getOrCreate(final String text)
    {
        if ( headerOptionMap.containsKey( text.toLowerCase() ) )
        {
            return headerOptionMap.get( text.toLowerCase() );
        }
        if ( CoreConfig.debug )
        {
            Logger.debug( "View requested unknown header [%s] - Creating...", text );
        }
        return create( text );
    }

    private HeaderOption cacheApplicable(final boolean b)
    {
        this.cacheApplicable = b;
        return this;
    }

    @Override
    public final String toString()
    {
        return this.text;
    }

}
