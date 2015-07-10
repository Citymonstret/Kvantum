package com.intellectualsites.web.object.filter;

import com.intellectualsites.web.object.syntax.Filter;

public class Lowercase extends Filter {

    public Lowercase() {
        super("lowercase");
    }

    public Object handle(Object in) {
        return in.toString().toLowerCase();
    }

}
