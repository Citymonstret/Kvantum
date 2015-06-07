package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.object.*;
import com.intellectualsites.web.object.filter.List;
import com.intellectualsites.web.object.filter.Lowercase;
import com.intellectualsites.web.object.filter.Uppercase;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Variable extends Syntax {

    private Map<String, Filter> filters;

    public Variable() {
        super(Pattern.compile("\\{\\{([a-zA-Z0-9]*)\\.([@A-Za-z0-9_\\-]*)( [|]{2} [A-Z]*)?\\}\\}"));
        filters = new HashMap<>();
        Set<Filter> preFilters = new LinkedHashSet<>();
        preFilters.add(new Uppercase());
        preFilters.add(new Lowercase());
        preFilters.add(new List());
        for (final Filter filter : preFilters) {
            filters.put(filter.toString(), filter);
        }
    }

    @Override
    public String process(String content, Matcher matcher, Request r, Map<String, ProviderFactory> factories) {
        while (matcher.find()) {
            String provider = matcher.group(1);
            String variable = matcher.group(2);

            String filter = "";
            if (matcher.group().contains(" || ")) {
                filter = matcher.group().split(" \\|\\| ")[1].replace("}}", "");
            }

            if (factories.containsKey(provider.toLowerCase())) {
                VariableProvider p = factories.get(provider.toLowerCase()).get(r);
                if (p != null) {
                    if (p.contains(variable)) {
                        Object o = p.get(variable);
                        if (!filter.equals("")) {
                            o = filters.get(filter.toUpperCase()).handle(o);
                        }
                        content = content.replace(matcher.group(), o.toString());
                    }
                } else {
                    content = content.replace(matcher.group(), "");
                }
            } else {
                content = content.replace(matcher.group(), "");
            }
        }
        return content;
    }
}
