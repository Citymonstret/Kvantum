/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
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
package com.github.intellectualsites.kvantum.api.request.post;

import com.github.intellectualsites.kvantum.api.request.AbstractRequest;
import com.github.intellectualsites.kvantum.api.request.RequestChild;
import com.github.intellectualsites.kvantum.api.util.Assert;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public abstract class PostRequest implements RequestChild
{

    /*
    TODO: Support more POST request body types, such as:
    - JSON
     */

    @Getter
    private final AbstractRequest parent;
    @Getter(AccessLevel.PROTECTED)
    private final String rawRequest;
    @Getter
    private final Map<String, String> variables;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String request;
    private boolean loaded;

    protected PostRequest(final AbstractRequest parent, final String rawRequest, boolean lazyLoad)
    {
        Assert.notNull( parent );
        Assert.notNull( rawRequest );

        this.loaded = false;
        this.parent = parent;
        this.rawRequest = rawRequest;
        this.request = rawRequest; // Can be overridden
        this.variables = new HashMap<>();
        if ( !lazyLoad )
        {
            this.load();
        }
    }

    private void load()
    {
        this.parseRequest( rawRequest );
        this.loaded = true;
    }

    private void checkIfShouldLoad()
    {
        if ( this.loaded )
        {
            return;
        }
        this.load();
    }

    protected abstract void parseRequest(String rawRequest);

    public abstract EntityType getEntityType();

    /**
     * Get a parameter
     *
     * @param key Parameter key
     * @return Parameter value if found, else null
     */
    public String get(final String key)
    {
        Assert.notNull( key );

        this.checkIfShouldLoad();

        if ( !this.getVariables().containsKey( key ) )
        {
            return null;
        }
        return this.getVariables().get( key );
    }

    /**
     * Check if a parameter is stored
     *
     * @param key Parameter key
     * @return True of the parameter is stored; else false
     */
    public boolean contains(final String key)
    {
        Assert.notNull( key );

        this.checkIfShouldLoad();

        return this.getVariables().containsKey( key );
    }

    /**
     * Get a copy of the internal parameter map
     *
     * @return copy of the internal map
     */
    final public Map<String, String> get()
    {
        this.checkIfShouldLoad();

        return new HashMap<>( this.variables );
    }

}
