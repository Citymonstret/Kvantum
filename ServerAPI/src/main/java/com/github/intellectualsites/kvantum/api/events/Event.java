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
package com.github.intellectualsites.kvantum.api.events;

import com.github.intellectualsites.kvantum.api.util.Assert;
import lombok.Getter;

/**
 * This is an event, in other words:
 * <br/>
 * Something that happens, that can be captured
 * using code
 */
public abstract class Event
{

    @Getter
    private final String name;
    private final int accessor;

    /**
     * The name which will be used
     * to identity this event
     *
     * @param name Event Name
     */
    protected Event(final String name)
    {
        Assert.notEmpty( name );

        this.name = name;
        this.accessor = getClass().getName().hashCode();
    }

    @Override
    public final String toString()
    {
        return this.getClass().getName();
    }

    @Override
    public final int hashCode()
    {
        return this.accessor;
    }

    @Override
    public final boolean equals(final Object o)
    {
        return o instanceof Event && ( (Event) o ).getName().equals( getName() );
    }
}
