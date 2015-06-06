package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.object.ProviderFactory;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Syntax;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Comment extends Syntax {

    public Comment() {
        super(Pattern.compile("(/\\*[\\S\\s]*?\\*/)"));
    }

    @Override
    public String process(String in, Matcher matcher, Request r, Map<String, ProviderFactory> factories) {
        while (matcher.find()) {
            in = in.replace(matcher.group(1), "");
        }
        return in;
    }
}
