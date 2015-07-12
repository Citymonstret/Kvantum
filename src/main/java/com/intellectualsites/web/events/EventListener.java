//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualsites.web.events;

import com.intellectualsites.web.util.Assert;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.ParameterizedType;

/**
 * This is a class which listens
 * to one specific event
 *
 * @param <T> Event type, that the listener should listen to
 */
public abstract class EventListener<T extends Event> {

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
    public EventListener() {
        this(EventPriority.MEDIUM);
    }

    /**
     * The constructor
     *
     * @param priority The priority in which the event should be called {@see EventPriority}
     */
    @SuppressWarnings("ALL")
    public EventListener(@NotNull final EventPriority priority) {
        Assert.notNull(priority);

        this.listenTo = ((Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0]).getName();
        this.accessor = this.listenTo.hashCode();
        this.priority = priority;
    }

    /**
     * Get the specified event priority
     *
     * @return Event Priority
     */
    public final EventPriority getPriority() {
        return this.priority;
    }

    /**
     * Listen to an incoming event
     *
     * @param t Incoming event
     */
    public abstract void listen(@NotNull final T t);

    /**
     * IGNORE
     *
     * @param q An object
     */
    @SuppressWarnings("ALL")
    final public void sxsdd(final Object q) {
        this.y = q;
    }

    /**
     * IGNORE
     *
     * @return An object
     */
    final public Object sddww() {
        return this.y;
    }

    /**
     * Get the class name of the class
     * that the event listens to
     *
     * @return Class name of the T class
     */
    final String listeningTo() {
        return this.listenTo;
    }

    @Override
    final public String toString() {
        return this.listeningTo();
    }

    @Override
    final public int hashCode() {
        return this.accessor;
    }
}
