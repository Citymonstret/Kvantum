package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Syntax;
import com.intellectualsites.web.object.VariableProvider;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfStatement extends Syntax {

    public IfStatement() {
        super(Pattern.compile("\\{(#if)( !| )([A-Za-z0-9]*).([A-Za-z0-9_\\-@]*)\\}([\\S\\s]*?)\\{(\\/if)\\}"));
    }

    @Override
    public String process(String in, Matcher matcher, Request r, Map<String, ProviderFactory> factories) {
        while (matcher.find()) {
            String neg = matcher.group(2), namespace = matcher.group(3), variable = matcher.group(4);
            if (factories.containsKey(namespace.toLowerCase())) {
                VariableProvider p = factories.get(namespace.toLowerCase()).get(r);
                if (p != null) {
                    if (p.contains(variable)) {
                        Object o = p.get(variable);
                        boolean b;
                        if (o instanceof Boolean) {
                            b = (Boolean) o;
                        } else if (o instanceof String) {
                            b = o.toString().toLowerCase().equals("true");
                        } else
                            b = o instanceof Number && ((Number) o).intValue() == 1;
                        if (neg.contains("!")) {
                            b = !b;
                        }

                        if (b) {
                            in = in.replace(matcher.group(), matcher.group(5));
                        } else {
                            in = in.replace(matcher.group(), "");
                        }
                    }
                }
            }
        }
        return in;
    }
}
