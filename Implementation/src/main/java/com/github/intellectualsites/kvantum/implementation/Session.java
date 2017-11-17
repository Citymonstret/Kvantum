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
package com.github.intellectualsites.kvantum.implementation;

import com.github.intellectualsites.kvantum.api.session.ISession;
import com.github.intellectualsites.kvantum.api.util.Assert;
import com.github.intellectualsites.kvantum.api.util.VariableProvider;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(of = { "sessionId", "sessionKey" })
@SuppressWarnings("unused")
final class Session implements ISession, VariableProvider
{

    private static long id = 0L;
    private final Map<String, Object> sessionStorage;
    @Getter
    private long sessionId = 0;

    @Setter
    @Getter
    private String sessionKey;

    Session()
    {
        this.sessionKey = UUID.randomUUID().toString();
        this.sessionStorage = new HashMap<>();
        this.sessionId = id++;
        this.sessionStorage.put( "id", "n/a" );
    }

    @Override
    public boolean contains(final String variable)
    {
        return sessionStorage.containsKey( Assert.notNull( variable ).toLowerCase() );
    }

    @Override
    public Object get(final String variable)
    {
        return sessionStorage.get( Assert.notNull( variable ).toLowerCase() );
    }

    @Override
    public Map<String, Object> getAll()
    {
        return new HashMap<>( sessionStorage );
    }

    @Override
    public ISession set(final String s, final Object o)
    {
        Assert.notNull( s );

        if ( o == null )
        {
            sessionStorage.remove( s );
        } else
        {
            sessionStorage.put( s.toLowerCase(), o );
        }

        return this;
    }

}
