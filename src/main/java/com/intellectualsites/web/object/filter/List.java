package com.intellectualsites.web.object.filter;

import com.intellectualsites.web.object.syntax.Filter;

import java.util.Collection;

public class List extends Filter {

    public List() {
        super("list");
    }

    public Object handle(Object o) {
        StringBuilder s = new StringBuilder();
        s.append("<ul>");
        if (o instanceof Object[]) {
            for (Object oo : (Object[]) o) {
                s.append("<li>").append(oo).append("</li>");
            }
        } else if (o instanceof Collection) {
            for (Object oo : (Collection) o) {
                s.append("<li>").append(oo).append("</li>");
            }
        }
        s.append("</ul>");
        return s.toString();
    }

}
