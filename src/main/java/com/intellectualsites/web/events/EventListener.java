package com.intellectualsites.web.events;

import java.lang.reflect.ParameterizedType;

/**
 * Created 2015-05-03 for IntellectualServer
 *
 * @author Citymonstret
 */
public abstract class EventListener<T extends Event> {

    private final String listenTo;
    private final int accessor;
    private final EventPriority priority;

    private Object y = null;

    public EventListener() {
        this(EventPriority.MEDIUM);
    }

    public EventListener(final EventPriority priority) {
        this.listenTo = ((Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0]).getName();
        this.accessor = this.listenTo.hashCode();
        this.priority = priority;
    }

    public final EventPriority getPriority() {
        return this.priority;
    }

    public abstract void listen(final T t);

    final public void sxsdd(final Object q) {
        this.y = q;
    }

    final public Object sddww() {
        return this.y;
    }

    final public String listeningTo() {
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
