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
package xyz.kvantum.server.api.events;

import xyz.kvantum.server.api.util.Assert;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The event manager
 */
@SuppressWarnings("unused")
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

    public Collection<EventListener> getAll(final Object y)
    {
        Assert.notNull( y );

        final List<EventListener> l = new ArrayList<>();
        for ( final Deque<EventListener> listeners : this.listeners.values() )
        {
            l.addAll( listeners.stream().filter( listener ->
                    listener.sddww().equals( y ) ).collect( Collectors.toList() ) );
        }
        return l;
    }

    public void removeAll(final Object y)
    {
        Assert.notNull( y );

        for ( final Deque<EventListener> listeners : this.listeners.values() )
        {
            listeners.stream().filter( listener ->
                    listener.sddww().equals( y ) ).forEachOrdered( listeners::remove );
        }
        bake();
    }

    public void removeListener(final EventListener listener)
    {
        Assert.notNull( listener );

        for ( final Deque<EventListener> ll : listeners.values() )
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
        List<EventListener> low;
        List<EventListener> med;
        List<EventListener> hig;
        int index;
        EventListener[] array;
        for ( final Map.Entry<Integer, ArrayDeque<EventListener>> entry : listeners
                .entrySet() )
        {
            low = new ArrayList<>();
            med = new ArrayList<>();
            hig = new ArrayList<>();
            for ( final EventListener listener : entry.getValue() )
            {
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
            }
            array = new EventListener[ low.size() + med.size() + hig.size() ];
            index = 0;
            for ( final EventListener listener : low )
            {
                array[ index++ ] = listener;
            }
            for ( final EventListener listener : med )
            {
                array[ index++ ] = listener;
            }
            for ( final EventListener listener : hig )
            {
                array[ index++ ] = listener;
            }
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
        for ( final EventListener listener : bakedListeners
                .get( event.hashCode() ) )
        {
            listener.listen( event );
        }
    }


}
