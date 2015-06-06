package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Syntax;
import com.intellectualsites.web.object.VariableProvider;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Variable extends Syntax {

    public Variable() {
        super(Pattern.compile("\\{\\{([a-zA-Z0-9]*)\\.([@A-Za-z0-9_\\-]*)( [|]{2} [A-Z]*)?\\}\\}"));
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
                            switch (filter) {
                                case "UPPERCASE":
                                    o = o.toString().toUpperCase();
                                    break;
                                case "LOWERCASE":
                                    o = o.toString().toLowerCase();
                                    break;
                                case "LIST": {
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
                                    o = s.toString();
                                }
                                break;
                                default:
                                    break;
                            }
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
