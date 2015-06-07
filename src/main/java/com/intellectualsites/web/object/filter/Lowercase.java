package com.intellectualsites.web.object.filter;

import com.intellectualsites.web.object.Filter;

public class Lowercase extends Filter {

    public Lowercase() {
        super("lowercase");
    }

    public Object handle(Object in) {
        return in.toString().toLowerCase();
    }

}
