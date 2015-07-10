package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.object.Request;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaBlockStatement extends Syntax {

    public MetaBlockStatement() {
        super(Pattern.compile("\\[([A-Za-z0-9]*):[ ]?([\\S\\s]*?)\\]"));
    }

    @Override
    public String process(String in, Matcher matcher, Request r, Map<String, ProviderFactory> factories) {
        while (matcher.find()) {
            // Document meta :D
            r.addMeta("doc." + matcher.group(1), matcher.group(2));
        }
        return in;
    }
}
