/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.iserver.api.events;

import com.github.intellectualsites.iserver.api.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The event manager
 */
@SuppressWarnings("unused")
public class EventManager
{

    private static EventManager instance;
    private final Map<Integer, ArrayDeque<com.github.intellectualsites.iserver.api.events.EventListener>> listeners;
    private Map<Integer, com.github.intellectualsites.iserver.api.events.EventListener[]> bakedListeners;

    private EventManager()
    {
        listeners = new HashMap<>();
    }

    public static EventManager getInstance()
    {
        if ( instance == null )
        {
            instance = new EventManager();
        }
        return instance;
    }

    public void addListener(final com.github.intellectualsites.iserver.api.events.EventListener listener)
    {
        Assert.notNull( listener );

        if ( !listeners.containsKey( listener.hashCode() ) )
        {
            listeners.put( listener.hashCode(), new ArrayDeque<>() );
        }
        listeners.get( listener.hashCode() ).add( listener );

        if ( !bakedListeners.isEmpty() )
        {
            bake(); // re-bake!
        }
    }

    public Collection<com.github.intellectualsites.iserver.api.events.EventListener> getAll(final Object y)
    {
        Assert.notNull( y );

        final List<com.github.intellectualsites.iserver.api.events.EventListener> l = new ArrayList<>();
        for ( final Deque<com.github.intellectualsites.iserver.api.events.EventListener> listeners : this.listeners.values() )
        {
            l.addAll( listeners.stream().filter( listener ->
                    listener.sddww().equals( y ) ).collect( Collectors.toList() ) );
        }
        return l;
    }

    public void removeAll(final Object y)
    {
        Assert.notNull( y );

        for ( final Deque<com.github.intellectualsites.iserver.api.events.EventListener> listeners : this.listeners.values() )
        {
            listeners.stream().filter( listener ->
                    listener.sddww().equals( y ) ).forEachOrdered( listeners::remove );
        }
        bake();
    }

    public void removeListener(final com.github.intellectualsites.iserver.api.events.EventListener listener)
    {
        Assert.notNull( listener );

        for ( final Deque<com.github.intellectualsites.iserver.api.events.EventListener> ll : listeners.values() )
        {
            ll.remove( listener );
        }
    }

    public void handle(final Event event)
    {
        Assert.notNull( event );

        call( event );
    }

    public void bake()
    {

        bakedListeners = new HashMap<>();
        List<com.github.intellectualsites.iserver.api.events.EventListener> low, med, hig;
        int index;
        com.github.intellectualsites.iserver.api.events.EventListener[] array;
        for ( final Map.Entry<Integer, ArrayDeque<com.github.intellectualsites.iserver.api.events.EventListener>> entry : listeners
                .entrySet() )
        {
            low = new ArrayList<>();
            med = new ArrayList<>();
            hig = new ArrayList<>();
            for ( final com.github.intellectualsites.iserver.api.events.EventListener listener : entry.getValue() )
                switch ( listener.getPriority() )
                {
                    case LOW:
                        low.add( listener );
                        break;
                    case MEDIUM:
                        low.add( listener );
                        break;
                    case HIGH:
                        hig.add( listener );
                        break;
                    default:
                        break;
                }
            array = new com.github.intellectualsites.iserver.api.events.EventListener[ low.size() + med.size() + hig.size() ];
            index = 0;
            for ( final com.github.intellectualsites.iserver.api.events.EventListener listener : low )
                array[ index++ ] = listener;
            for ( final com.github.intellectualsites.iserver.api.events.EventListener listener : med )
                array[ index++ ] = listener;
            for ( final com.github.intellectualsites.iserver.api.events.EventListener listener : hig )
                array[ index++ ] = listener;
            bakedListeners.put( entry.getKey(), array );
        }

    }

    @SuppressWarnings("ALL")
    private void call(final Event event) throws NullPointerException
    {
        Assert.notNull( event );

        if ( bakedListeners == null
                || !bakedListeners.containsKey( event.hashCode() ) )
        {
            return;
        }
        for ( final com.github.intellectualsites.iserver.api.events.EventListener listener : bakedListeners
                .get( event.hashCode() ) )
        {
            listener.listen( event );
        }
    }


}
