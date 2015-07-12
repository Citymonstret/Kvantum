package com.intellectualsites.web.events;

import com.intellectualsites.web.util.Assert;

/**
 * This is an event, in other words:
 * <br/>
 * Something that happens, that can be captured
 * using code
 *
 * @author Citymonstret
 */
public abstract class Event {

    private final String name;
    private final int accessor;

    /**
     * The name which will be used
     * to identity this event
     *
     * @param name Event Name
     */
    protected Event(final String name) {
        Assert.notEmpty(name);

        this.name = name;
        this.accessor = getClass().getName().hashCode();
    }

    @Override
    public final String toString() {
        return this.getClass().getName();
    }

    /**
     * Get the specified event name
     *
     * @return Event name
     */
    private String getName() {
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
