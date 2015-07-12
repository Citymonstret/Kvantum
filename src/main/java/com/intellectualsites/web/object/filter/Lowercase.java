package com.intellectualsites.web.object.filter;

import com.intellectualsites.web.object.syntax.Filter;

public class Lowercase extends Filter {

    public Lowercase() {
        super("lowercase");
    }

    public Object handle(String objectName, Object in) {
        return in.toString().toLowerCase();
    }

}
