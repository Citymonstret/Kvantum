package com.intellectualsites.web.events;

import com.intellectualsites.web.object.EventCaller;

/**
 * Created 2015-05-04 for IntellectualServer
 *
 * @author Citymonstret
 */
public class DefaultEventCaller extends EventCaller {

    @Override
    public void callEvent(Event event) {
        EventManager.getInstance().handle(event);
    }

}
