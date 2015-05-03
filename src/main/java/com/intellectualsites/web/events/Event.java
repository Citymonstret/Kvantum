package com.intellectualsites.web.events;

/**
 * Created 2015-05-03 for IntellectualServer
 *
 * @author Citymonstret
 */
public abstract class Event {

    private final String name;
    private final int accessor;

    public Event(final String name) {
        this.name = name;
        this.accessor = getClass().getName().hashCode();
    }

    @Override
    public final String toString() {
        return this.getClass().getName();
    }

    public final String getName() {
        return this.name;
    }

    @Override
    public final int hashCode() {
        return this.accessor;
    }

    @Override
    public final boolean equals(final Object o) {
        return o instanceof Event && ((Event) o).getName().equals(getName());
    }
}
