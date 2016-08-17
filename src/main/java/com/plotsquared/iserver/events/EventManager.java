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

package com.plotsquared.iserver.events;

import com.plotsquared.iserver.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The event manager
 */
@SuppressWarnings( "unused" )
public class EventManager
{

    private static EventManager instance;
    private final Map<Integer, ArrayDeque<EventListener>> listeners;
    private Map<Integer, EventListener[]> bakedListeners;

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

    public void addListener(final EventListener listener)
    {
        Assert.notNull( listener );

        synchronized ( listeners )
        {
            if ( !listeners.containsKey( listener.hashCode() ) )
                listeners.put( listener.hashCode(),
                        new ArrayDeque<>() );
            listeners.get( listener.hashCode() ).add( listener );
        }
    }

    public Collection<EventListener> getAll(final Object y)
    {
        Assert.notNull( y );

        synchronized ( listeners )
        {
            final List<EventListener> l = new ArrayList<>();
            for ( final Deque<EventListener> listeners : this.listeners.values() )
            {
                l.addAll( listeners.stream().filter( listener ->
                        listener.sddww().equals( y ) ).collect( Collectors.toList() ) );
            }
            return l;
        }
    }

    public void removeAll(final Object y)
    {
        Assert.notNull( y );

        synchronized ( listeners )
        {
            for ( final Deque<EventListener> listeners : this.listeners.values() )
            {
                listeners.stream().filter( listener ->
                        listener.sddww().equals( y ) ).forEachOrdered( listeners::remove );
            }
            bake();
        }
    }

    public void removeListener(final EventListener listener)
    {
        Assert.notNull( listener );

        synchronized ( listeners )
        {
            for ( final Deque<EventListener> ll : listeners.values() )
                ll.remove( listener );
        }
    }

    public void handle(final Event event)
    {
        Assert.notNull( event );

        synchronized ( this )
        {
            call( event );
        }
    }

    public void bake()
    {
        synchronized ( this )
        {
            bakedListeners = new HashMap<>();
            List<EventListener> low, med, hig;
            int index;
            EventListener[] array;
            for ( final Map.Entry<Integer, ArrayDeque<EventListener>> entry : listeners
                    .entrySet() )
            {
                low = new ArrayList<>();
                med = new ArrayList<>();
                hig = new ArrayList<>();
                for ( final EventListener listener : entry.getValue() )
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
                array = new EventListener[ low.size() + med.size() + hig.size() ];
                index = 0;
                for ( final EventListener listener : low )
                    array[ index++ ] = listener;
                for ( final EventListener listener : med )
                    array[ index++ ] = listener;
                for ( final EventListener listener : hig )
                    array[ index++ ] = listener;
                bakedListeners.put( entry.getKey(), array );
            }
        }
    }

    @SuppressWarnings("ALL")
    private void call(final Event event) throws NullPointerException
    {
        Assert.notNull( event );

        if ( bakedListeners == null
                || !bakedListeners.containsKey( event.hashCode() ) )
            return;
        for ( final EventListener listener : bakedListeners
                .get( event.hashCode() ) )
            listener.listen( event );
    }


}
