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
package xyz.kvantum.server.implementation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;
import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.util.Assert;
import xyz.kvantum.server.api.util.VariableProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(of = { "sessionId", "sessionKey" })
@SuppressWarnings("unused")
public final class Session implements ISession, VariableProvider
{

    @Getter
    private static final KvantumPojoFactory<ISession> kvantumPojoFactory = KvantumPojoFactory
            .forClass( ISession.class );
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
        return sessionStorage.containsKey( Assert.notNull( variable ).toLowerCase( Locale.ENGLISH ) );
    }

    @Override
    public Object get(final String variable)
    {
        return sessionStorage.get( Assert.notNull( variable ).toLowerCase( Locale.ENGLISH ) );
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
            sessionStorage.put( s.toLowerCase( Locale.ENGLISH ), o );
        }

        return this;
    }

    @Override
    public KvantumPojo<ISession> toKvantumPojo()
    {
        return kvantumPojoFactory.of( this );
    }

}
