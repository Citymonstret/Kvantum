package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.object.Request;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForEachBlock extends Syntax {

    public ForEachBlock() {
        super(Pattern.compile("\\{#foreach ([A-Za-z0-9]*).([A-Za-z0-9]*) -> ([A-Za-z0-9]*)\\}([\\s\\S]*)\\{/foreach\\}"));
    }

    @Override
    public String process(String content, Matcher matcher, Request r, Map<String, ProviderFactory> factories) {
        while (matcher.find()) {
            String provider = matcher.group(1);
            String variable = matcher.group(2);
            String variableName = matcher.group(3);
            String forContent = matcher.group(4);

            if (factories.containsKey(provider.toLowerCase())) {
                VariableProvider p = factories.get(provider.toLowerCase()).get(r);
                if (p != null) {
                    if (!p.contains(variable)) {
                        content = content.replace(matcher.group(), "");
                    } else {
                        Object o = p.get(variable);

                        StringBuilder totalContent = new StringBuilder();
                        if (o instanceof Object[]) {
                            for (Object oo : (Object[]) o) {
                                totalContent.append(forContent.replace("{{" + variableName + "}}", oo.toString()));
                            }
                        } else if (o instanceof Collection) {
                            for (Object oo : (Collection) o) {
                                totalContent.append(forContent.replace("{{" + variableName + "}}", oo.toString()));
                            }
                        }
                        content = content.replace(matcher.group(), totalContent.toString());
                    }
                } else {
                    content = content.replace(matcher.group(), "");
                }
            }  else {
                content = content.replace(matcher.group(), "");
            }
        }
        return content;
    }
}
