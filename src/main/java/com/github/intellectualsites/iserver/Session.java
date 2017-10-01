/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver;

import com.github.intellectualsites.iserver.api.session.ISession;
import com.github.intellectualsites.iserver.api.util.Assert;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
final class Session implements ISession
{

    private static long id = 0L;
    private final Map<String, Object> sessionStorage;
    @Getter
    private long sessionId = 0;

    Session()
    {
        sessionStorage = new HashMap<>();
        sessionId = id++;
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
    public void set(final String s, final Object o)
    {
        Assert.notNull( s );

        if ( o == null )
        {
            sessionStorage.remove( s );
        } else
        {
            sessionStorage.put( s.toLowerCase(), o );
        }
    }

}
