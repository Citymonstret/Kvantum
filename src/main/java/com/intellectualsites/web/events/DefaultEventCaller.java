package com.intellectualsites.web.events;

import com.intellectualsites.web.object.EventCaller;

/**
 * This is the default event caller
 *
 * @author Citymonstret
 */
public class DefaultEventCaller extends EventCaller {

    @Override
    public void callEvent(Event event) {
        EventManager.getInstance().handle(event);
    }

}
