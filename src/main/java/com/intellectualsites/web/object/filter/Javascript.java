package com.intellectualsites.web.object.filter;

import com.intellectualsites.web.object.syntax.Filter;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created 2015-07-12 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Javascript extends Filter {

    public Javascript() {
        super("javascript");
    }

    public Object handle(String objectName, Object o) {
        StringBuilder s = new StringBuilder();
        s.append("var ").append(objectName).append(" = ");
        if (o instanceof Object[]) {
            Object[] oo = (Object[]) o;
            s.append("[\n");
            Iterator iterator = Arrays.asList(oo).iterator();
            while (iterator.hasNext()) {
                Object ooo = iterator.next();
                handleObject(s, ooo);
                if (iterator.hasNext()) {
                    s.append(",\n");
                }
            }
            s.append("]");
        } else {
            handleObject(s, o);
        }
        return s.append(";").toString();
    }

    private void handleObject(StringBuilder s, Object o) {
        if (o instanceof Number || o instanceof Boolean) {
            s.append(o);
        } else if (o instanceof Object[]) {
            for (Object oo : (Object[]) o) {
                handleObject(s, oo);
            }
        } else {
            s.append("\"").append(o.toString()).append("\"");
        }
    }
}
