package com.intellectualsites.web.events;

import com.intellectualsites.web.events.Event;

/**
 * This handles calling
 *
 * @author Citymonstret
 */
public abstract class EventCaller {

    /**
     * Call an event
     *
     * @param event Event to call
     */
    public abstract void callEvent(final Event event);

}
