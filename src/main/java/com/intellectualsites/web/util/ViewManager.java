package com.intellectualsites.web.util;

import com.intellectualsites.web.core.Server;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.View;
import com.intellectualsites.web.views.errors.View404;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class ViewManager {

    private List<View> views;

    public ViewManager() {
        this.views = new ArrayList<View>();
    }

    public void add(final View view) {
        for (View v : views) {
            if (v.toString().equalsIgnoreCase(view.toString())) {
                throw new IllegalArgumentException("Duplicate view pattern!");
            }
        }
        views.add(view);
    }

    public View match(final Request request) {
        for (View v : views) {
            if (v.matches(request)) {
                return v;
            }
        }
        return new View404();
    }

    public void dump(final Server server) {
        for (View view : views) {
            server.log("> View - Class '%s', Regex: '%s'\n\tOptions: %s", view.getClass().getSimpleName(), view.toString(), view.getOptionString());
        }
    }

    public void remove(View view) {
        if (views.contains(view)) {
            views.remove(view);
        }
    }
}
