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

import java.lang.reflect.ParameterizedType;

/**
 * This is a class which listens
 * to one specific event
 *
 * @param <T> Event type, that the listener should listen to
 */
public abstract class EventListener<T extends Event>
{

    private final String listenTo;
    private final int accessor;
    private final EventPriority priority;

    private Object y = null;

    /**
     * A constructor which defaults to the medium event priority
     *
     * @see #EventListener(EventPriority) to choose another priority
     */
    @SuppressWarnings("ALL")
    public EventListener()
    {
        this( EventPriority.MEDIUM );
    }

    /**
     * The constructor
     *
     * @param priority The priority in which the event should be called {@see EventPriority}
     */
    @SuppressWarnings("ALL")
    public EventListener(final EventPriority priority)
    {
        Assert.notNull( priority );

        this.listenTo = ( (Class<T>) ( (ParameterizedType) getClass()
                .getGenericSuperclass() ).getActualTypeArguments()[ 0 ] ).getName();
        this.accessor = this.listenTo.hashCode();
        this.priority = priority;
    }

    /**
     * Get the specified event priority
     *
     * @return Event Priority
     */
    public final EventPriority getPriority()
    {
        return this.priority;
    }

    /**
     * Listen to an incoming event
     *
     * @param t Incoming event
     */
    public abstract void listen(final T t);

    /**
     * IGNORE
     *
     * @param q An object
     */
    @SuppressWarnings("ALL")
    final public void sxsdd(final Object q)
    {
        this.y = q;
    }

    /**
     * IGNORE
     *
     * @return An object
     */
    final public Object sddww()
    {
        return this.y;
    }

    /**
     * Get the class name of the class
     * that the event listens to
     *
     * @return Class name of the T class
     */
    final String listeningTo()
    {
        return this.listenTo;
    }

    @Override
    final public String toString()
    {
        return this.listeningTo();
    }

    @Override
    final public int hashCode()
    {
        return this.accessor;
    }
}
