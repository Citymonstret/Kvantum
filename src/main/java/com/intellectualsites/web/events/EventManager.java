package com.intellectualsites.web.events;

import com.intellectualsites.web.util.Assert;

import java.util.*;

/**
 * The event manager
 *
 * @author Citymonstret
 */
public class EventManager {

    private static EventManager instance;
    private final Map<Integer, ArrayDeque<EventListener>> listeners;
    private Map<Integer, EventListener[]> bakedListeners;

    private EventManager() {
        listeners = new HashMap<>();
    }

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public Collection<EventListener> getAll(final Object y) {
        synchronized (listeners) {
            final List<EventListener> l = new ArrayList<>();
            for (final Deque<EventListener> listeners : this.listeners.values()) {
                for (final EventListener listener : listeners) {
                    if (listener.sddww().equals(y)) {
                        l.add(listener);
                    }
                }
            }
            return l;
        }
    }

    public void removeAll(final Object y) {
        synchronized (listeners) {
            for (final Deque<EventListener> listeners : this.listeners.values()) {
                for (final EventListener listener : listeners) {
                    if (listener.sddww().equals(y)) {
                        listeners.remove(listener);
                    }
                }
            }
            bake();
        }
    }

    public void removeListener(final EventListener listener) {
        Assert.notNull(listener);
        synchronized (listeners) {
            for (final Deque<EventListener> ll : listeners.values())
                ll.remove(listener);
        }
    }

    public void handle(final Event event) {
        synchronized (this) {
            call(event);
        }
    }

    public void bake() {
        synchronized (this) {
            bakedListeners = new HashMap<>();
            List<EventListener> low, med, hig;
            int index;
            EventListener[] array;
            for (final Map.Entry<Integer, ArrayDeque<EventListener>> entry : listeners
                    .entrySet()) {
                low = new ArrayList<>();
                med = new ArrayList<>();
                hig = new ArrayList<>();
                for (final EventListener listener : entry.getValue())
                    switch (listener.getPriority()) {
                        case LOW:
                            low.add(listener);
                            break;
                        case MEDIUM:
                            low.add(listener);
                            break;
                        case HIGH:
                            hig.add(listener);
                            break;
                        default:
                            break;
                    }
                array = new EventListener[low.size() + med.size() + hig.size()];
                index = 0;
                for (final EventListener listener : low)
                    array[index++] = listener;
                for (final EventListener listener : med)
                    array[index++] = listener;
                for (final EventListener listener : hig)
                    array[index++] = listener;
                bakedListeners.put(entry.getKey(), array);
            }
        }
    }

    @SuppressWarnings("ALL")
    private void call(final Event event) throws NullPointerException {
        Assert.notNull(event);
        if (bakedListeners == null
                || !bakedListeners.containsKey(event.hashCode()))
            return;
        for (final EventListener listener : bakedListeners
                .get(event.hashCode()))
            listener.listen(event);
    }


}
