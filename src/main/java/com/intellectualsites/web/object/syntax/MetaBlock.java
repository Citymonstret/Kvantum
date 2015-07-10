package com.intellectualsites.web.object.syntax;

import com.intellectualsites.web.object.Request;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaBlock extends Syntax {

    private MetaBlockStatement statement;

    public MetaBlock() {
        super(Pattern.compile("\\{\\{:([\\S\\s]*?):\\}\\}"));
        this.statement = new MetaBlockStatement();
    }

    @Override
    public String process(String in, Matcher matcher, Request r, Map<String, ProviderFactory> factories) {
        while (matcher.find()) {
            String blockContent = matcher.group(1);
            statement.handle(in, r, factories);
            in = in.replace(matcher.group(), "");
        }
        return in;
    }
}
