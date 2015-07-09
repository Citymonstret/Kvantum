package com.intellectualsites.web.views;

import com.intellectualsites.web.config.YamlConfiguration;
import com.intellectualsites.web.object.syntax.IgnoreSyntax;
import com.intellectualsites.web.object.Request;
import com.intellectualsites.web.object.Response;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

public class RedirectView extends View implements IgnoreSyntax {

    private final YamlConfiguration configuration;

    public RedirectView(String pattern, Map<String, Object> options) {
        super(pattern, "redirect", options);
        super.relatedFolderPath = "/redirect";

        File file = new File(getFolder(), "redirect.yml");

        try {
            configuration = new YamlConfiguration("redirect", file);
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }

        configuration.loadFile();
    }

    @Override
    public boolean passes(Matcher matcher, Request request) {
        if (configuration.contains(matcher.group(1))) {
            request.addMeta("redirect_url", configuration.get(matcher.group(1)));
            return true;
        }
        return false;
    }

    @Override
    public Response generate(final Request r) {
        Response response = new Response(this);
        response.getHeader().redirect(r.getMeta("redirect_url").toString());
        return response;
    }
}
