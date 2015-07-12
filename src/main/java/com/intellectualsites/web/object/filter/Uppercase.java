package com.intellectualsites.web.object.filter;

import com.intellectualsites.web.object.syntax.Filter;

public class Uppercase extends Filter {

    public Uppercase() {
        super("uppercase");
    }

    @Override
    public Object handle(String objectName, Object in) {
        return in.toString().toUpperCase();
    }

}
